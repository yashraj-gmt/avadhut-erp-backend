// com/erp/system/mapper/ProductMapper.java
package com.erp.system.mapper;

import com.erp.system.dto.response.ProductImageResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.entity.Inventory;
import com.erp.system.entity.Product;
import com.erp.system.entity.ProductImage;
import com.erp.system.repository.InventoryRepository;
import com.erp.system.repository.ProductImageRepository;
import com.erp.system.service.FileUploadService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Abstract class (not interface) so we can @Autowired Spring beans for
 * computed fields that MapStruct cannot derive from the entity alone.
 */
@Mapper(componentModel = "spring")
public abstract class ProductMapper {

    @Autowired protected FileUploadService        fileUploadService;
    @Autowired protected ProductImageRepository   imageRepository;
    @Autowired protected InventoryRepository      inventoryRepository;
    @Autowired protected InventoryMapper          inventoryMapper;

    // ── Detail response ───────────────────────────────────────────────────

    @Mapping(source = "category.id",   target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "qrCodeUrl",    ignore = true)
    @Mapping(target = "images",       ignore = true)
    @Mapping(target = "inventories",  ignore = true)
    public abstract ProductResponse toDetailResponse(Product product);

    @AfterMapping
    protected void enrichDetailResponse(Product p,
                                        @MappingTarget ProductResponse r) {
        // QR code URL
        r.setQrCodeUrl(fileUploadService.toPublicUrl(p.getQrCodeImage()));

        // Gallery images
        List<ProductImage> imgs =
                imageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId());
        r.setImages(imgs.stream().map(this::toImageResponse).toList());

        // Inventory records across warehouses
        List<Inventory> inventories = inventoryRepository.findByProductId(p.getId());
        r.setInventories(inventories.stream().map(inventoryMapper::toResponse).toList());
    }

    // ── Summary response (list) ───────────────────────────────────────────

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "primaryImageUrl", ignore = true)
    public abstract ProductSummaryResponse toSummaryResponse(Product product);

    @AfterMapping
    protected void enrichSummaryResponse(Product p,
                                         @MappingTarget ProductSummaryResponse r) {
        String url = imageRepository
                .findByProductIdAndIsPrimaryTrue(p.getId())
                .map(img -> fileUploadService.toPublicUrl(img.getImageUrl()))
                .orElse(null);
        r.setPrimaryImageUrl(url);
    }

    // ── Image response ────────────────────────────────────────────────────

    /** imageUrl is a relative path on entity; convert to public URL via service. */
    @Mapping(target = "imageUrl", ignore = true)
    public abstract ProductImageResponse toImageResponse(ProductImage image);

    @AfterMapping
    protected void enrichImageResponse(ProductImage img,
                                       @MappingTarget ProductImageResponse r) {
        r.setImageUrl(fileUploadService.toPublicUrl(img.getImageUrl()));
    }
}