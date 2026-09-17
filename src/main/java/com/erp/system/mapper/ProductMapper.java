package com.erp.system.mapper;

import com.erp.system.dto.response.ProductImageResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.entity.Product;
import com.erp.system.entity.ProductImage;
import com.erp.system.repository.ProductImageRepository;
import com.erp.system.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Product / ProductImage entities to response DTOs.
 *
 * Implemented as a Spring @Component to guarantee bean availability
 * regardless of IDE or build-tool annotation processing configuration.
 */
@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final FileUploadService      fileUploadService;
    private final ProductImageRepository imageRepository;

    public ProductResponse toDetailResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .productCode(product.getProductCode())
                .purchasePrice(product.getPurchasePrice())
                .rentPrice(product.getRentPrice())
                .currentStock(product.getCurrentStock())
                .description(product.getDescription())
                .productBy(product.getProductBy())
                .isActive(product.getIsActive())
                .images(toImageResponseList(product))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public ProductSummaryResponse toSummaryResponse(Product product) {
        if (product == null) {
            return null;
        }

        return ProductSummaryResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .productCode(product.getProductCode())
                .purchasePrice(product.getPurchasePrice())
                .rentPrice(product.getRentPrice())
                .currentStock(product.getCurrentStock())
                .productBy(product.getProductBy())
                .isActive(product.getIsActive())
                .primaryImageUrl(resolvePrimaryImageUrl(product))
                .createdAt(product.getCreatedAt())
                .build();
    }

    public ProductImageResponse toImageResponse(ProductImage image) {
        if (image == null) {
            return null;
        }

        return ProductImageResponse.builder()
                .id(image.getId())
                .imageUrl(fileUploadService != null ? fileUploadService.toPublicUrl(image.getImageUrl()) : image.getImageUrl())
                .originalFileName(image.getOriginalFileName())
                .fileSize(image.getFileSize())
                .isPrimary(image.getIsPrimary())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    protected List<ProductImageResponse> toImageResponseList(Product product) {
        if (product == null || product.getId() == null) {
            return Collections.emptyList();
        }
        return imageRepository
                .findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
    }

    protected String resolvePrimaryImageUrl(Product product) {
        if (product == null || product.getId() == null) {
            return null;
        }
        return imageRepository
                .findByProductIdAndIsPrimaryTrue(product.getId())
                .map(img -> fileUploadService != null ? fileUploadService.toPublicUrl(img.getImageUrl()) : img.getImageUrl())
                .orElse(null);
    }
}