// com/erp/system/service/impl/ProductServiceImpl.java
package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateProductRequest;
import com.erp.system.dto.request.UpdateProductRequest;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.entity.*;
import com.erp.system.enums.ProductStatus;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.ProductMapper;
import com.erp.system.repository.*;
import com.erp.system.service.FileUploadService;
import com.erp.system.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository      productRepository;
    private final CategoryRepository     categoryRepository;
    private final ProductImageRepository imageRepository;
    private final InventoryRepository    inventoryRepository;
    private final WarehouseRepository    warehouseRepository;
    private final FileUploadService      fileUploadService;
    private final ProductMapper          productMapper;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request,
                                  List<MultipartFile> images,
                                  MultipartFile qrCode) {

        if (productRepository.existsByProductCodeIgnoreCase(request.getProductCode())) {
            throw new AppException(
                    "Product code '" + request.getProductCode() + "' is already in use.",
                    HttpStatus.CONFLICT
            );
        }

        Product product = new Product();
        applyProductFields(product, request);

        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (qrCode != null && !qrCode.isEmpty()) {
            product.setQrCodeImage(fileUploadService.uploadQrCode(qrCode));
        }

        Product saved = productRepository.save(product);

        if (images != null && !images.isEmpty()) {
            List<ProductImage> productImages = uploadProductImages(saved, images);
            if (!productImages.isEmpty()) productImages.get(0).setIsPrimary(true);
            imageRepository.saveAll(productImages);
        }

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Warehouse not found with id: " + request.getWarehouseId()));
            createInventoryRecord(saved, warehouse, request.getStockQuantity(),
                    request.getStockAlert(), request.getWarehouseCapacity());
            saved.setCurrentStock(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
            productRepository.save(saved);
        }

        log.info("Product created: id={}, code={}, status={}",
                saved.getId(), saved.getProductCode(), saved.getStatus());
        return productMapper.toDetailResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductSummaryResponse> getAll(String search, Long categoryId,
                                                        ProductStatus status, Boolean isActive,
                                                        Long warehouseId, Pageable pageable) {
        Page<Product> page = productRepository.findAllWithFilters(
                search, categoryId, status, isActive, warehouseId, pageable);
        return PagedResponse.from(page.map(productMapper::toSummaryResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toDetailResponse(findOrThrow(id));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request,
                                  List<MultipartFile> newImages, MultipartFile newQrCode) {

        Product product = findOrThrow(id);

        if (request.getProductCode() != null
                && !request.getProductCode().equalsIgnoreCase(product.getProductCode())
                && productRepository.existsByProductCodeIgnoreCaseAndIdNot(request.getProductCode(), id)) {
            throw new AppException(
                    "Product code '" + request.getProductCode() + "' is already in use.",
                    HttpStatus.CONFLICT
            );
        }

        applyProductPatch(product, request);

        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Soft-delete removed images
        if (request.getRemoveImageIds() != null && !request.getRemoveImageIds().isEmpty()) {
            List<ProductImage> toRemove =
                    imageRepository.findByIdsAndProductId(request.getRemoveImageIds(), id);
            toRemove.forEach(img -> {
                fileUploadService.deleteFile(img.getImageUrl());
                img.setDeleted(true);           // ← soft-delete image
            });
            imageRepository.saveAll(toRemove);
        }

        if (request.getSetPrimaryImageId() != null) {
            imageRepository.clearPrimaryForProduct(id);
            ProductImage primary = imageRepository.findById(request.getSetPrimaryImageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
            if (!primary.getProduct().getId().equals(id)) {
                throw new AppException("Image does not belong to this product.", HttpStatus.BAD_REQUEST);
            }
            primary.setIsPrimary(true);
            imageRepository.save(primary);
        }

        if (newImages != null && !newImages.isEmpty()) {
            long existingCount = imageRepository.countByProductId(id);
            List<ProductImage> added = uploadProductImages(product, newImages);
            if (existingCount == 0 && !added.isEmpty()) added.get(0).setIsPrimary(true);
            imageRepository.saveAll(added);
        }

        if (newQrCode != null && !newQrCode.isEmpty()) {
            if (product.getQrCodeImage() != null) fileUploadService.deleteFile(product.getQrCodeImage());
            product.setQrCodeImage(fileUploadService.uploadQrCode(newQrCode));
        }

        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Warehouse not found with id: " + request.getWarehouseId()));

            Inventory inv = inventoryRepository
                    .findByProductIdAndWarehouseId(id, request.getWarehouseId())
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setProduct(product);
                        newInv.setWarehouse(warehouse);
                        return newInv;
                    });

            if (request.getStockQuantity()     != null) inv.setStockQuantity(request.getStockQuantity());
            if (request.getStockAlert()        != null) inv.setStockAlert(request.getStockAlert());
            if (request.getWarehouseCapacity() != null) inv.setWarehouseCapacity(request.getWarehouseCapacity());
            inventoryRepository.save(inv);

            product.setCurrentStock(inv.getStockQuantity());
        }

        Product saved = productRepository.save(product);
        log.info("Product updated: id={}, status={}", saved.getId(), saved.getStatus());
        return productMapper.toDetailResponse(saved);
    }

    // ── Workflow ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ProductResponse publish(Long id) {
        Product product = findOrThrow(id);
        product.setStatus(ProductStatus.PUBLISHED);
        Product saved = productRepository.save(product);
        log.info("Product published: id={}", id);
        return productMapper.toDetailResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse revertToDraft(Long id) {
        Product product = findOrThrow(id);
        product.setStatus(ProductStatus.DRAFT);
        Product saved = productRepository.save(product);
        log.info("Product reverted to draft: id={}", id);
        return productMapper.toDetailResponse(saved);
    }

    // ── Soft-Delete ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findOrThrow(id);

        // Soft-delete all related images
        List<ProductImage> images = imageRepository.findByProductIdOrderByDisplayOrderAsc(id);
        images.forEach(img -> {
            fileUploadService.deleteFile(img.getImageUrl());
            img.setDeleted(true);
        });
        imageRepository.saveAll(images);

        // Delete QR file from disk (no soft-delete needed for QR path, it's just a column)
        if (product.getQrCodeImage() != null) {
            fileUploadService.deleteFile(product.getQrCodeImage());
            product.setQrCodeImage(null);
        }

        product.setDeleted(true);           // ← soft-delete product
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    @Override
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        findOrThrow(productId);
        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        if (!image.getProduct().getId().equals(productId)) {
            throw new AppException("Image does not belong to this product.", HttpStatus.BAD_REQUEST);
        }

        fileUploadService.deleteFile(image.getImageUrl());
        image.setDeleted(true);             // ← soft-delete image
        imageRepository.save(image);

        // Promote next image to primary if this was the primary
        if (Boolean.TRUE.equals(image.getIsPrimary())) {
            imageRepository.findByProductIdOrderByDisplayOrderAsc(productId)
                    .stream().findFirst()
                    .ifPresent(next -> {
                        next.setIsPrimary(true);
                        imageRepository.save(next);
                    });
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    private List<ProductImage> uploadProductImages(Product product, List<MultipartFile> files) {
        List<ProductImage> result    = new ArrayList<>();
        long               baseOrder = imageRepository.countByProductId(product.getId());

        IntStream.range(0, files.size()).forEach(i -> {
            MultipartFile file = files.get(i);
            if (file != null && !file.isEmpty()) {
                String path = fileUploadService.uploadImage(file, FileUploadService.DIR_PRODUCT_IMAGES);
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(path);
                img.setOriginalFileName(file.getOriginalFilename());
                img.setFileSize(file.getSize());
                img.setIsPrimary(false);
                img.setDisplayOrder((int) baseOrder + i);
                result.add(img);
            }
        });
        return result;
    }

    private void createInventoryRecord(Product product, Warehouse warehouse,
                                       Integer stockQty, Integer stockAlert,
                                       Integer warehouseCapacity) {
        Inventory inv = new Inventory();
        inv.setProduct(product);
        inv.setWarehouse(warehouse);
        inv.setStockQuantity(stockQty != null ? stockQty : 0);
        inv.setStockAlert(stockAlert);
        inv.setWarehouseCapacity(warehouseCapacity);
        inventoryRepository.save(inv);
    }

    private void applyProductFields(Product p, CreateProductRequest req) {
        p.setName(req.getName().trim());
        p.setProductCode(req.getProductCode().trim().toUpperCase());
        p.setUnit(req.getUnit());
        p.setPurchasePrice(req.getPurchasePrice());
        p.setSellingPrice(req.getSellingPrice());
        p.setRentPrice(req.getRentPrice());
        p.setMinimumStock(req.getMinimumStock() != null ? req.getMinimumStock() : 0);
        p.setCurrentStock(0);
        p.setHsnCode(req.getHsnCode());
        p.setGstPercent(req.getGstPercent());
        p.setDescription(req.getDescription());
        p.setIsActive(req.getIsActive() != null ? req.getIsActive() : true);
        p.setStatus(req.getStatus() != null ? req.getStatus() : ProductStatus.DRAFT);
    }

    private void applyProductPatch(Product p, UpdateProductRequest req) {
        if (req.getName()          != null) p.setName(req.getName().trim());
        if (req.getProductCode()   != null) p.setProductCode(req.getProductCode().trim().toUpperCase());
        if (req.getUnit()          != null) p.setUnit(req.getUnit());
        if (req.getPurchasePrice() != null) p.setPurchasePrice(req.getPurchasePrice());
        if (req.getSellingPrice()  != null) p.setSellingPrice(req.getSellingPrice());
        if (req.getRentPrice()     != null) p.setRentPrice(req.getRentPrice());
        if (req.getMinimumStock()  != null) p.setMinimumStock(req.getMinimumStock());
        if (req.getHsnCode()       != null) p.setHsnCode(req.getHsnCode());
        if (req.getGstPercent()    != null) p.setGstPercent(req.getGstPercent());
        if (req.getDescription()   != null) p.setDescription(req.getDescription());
        if (req.getIsActive()      != null) p.setIsActive(req.getIsActive());
        if (req.getStatus()        != null) p.setStatus(req.getStatus());
    }
}