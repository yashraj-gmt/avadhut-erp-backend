// com/erp/system/mapper/ProductMapper.java
package com.erp.system.mapper;

import com.erp.system.dto.response.ProductImageResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.entity.Product;
import com.erp.system.entity.ProductImage;
import com.erp.system.repository.ProductImageRepository;
import com.erp.system.service.FileUploadService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Product / ProductImage entities to response DTOs.
 *
 * All three response DTOs use Lombok @Builder, which means MapStruct generates code
 * that calls builder().build() and returns the immutable object — BEFORE any
 * @AfterMapping hook can mutate it via setters. Therefore we must NOT use @AfterMapping
 * here. Instead every computed field is wired directly via @Mapping(expression = "java(...)")
 * so it is set inside the builder chain, before build() is called.
 *
 * Rule: the expression parameter sees the method-parameter name exactly as declared
 * in the abstract method signature (e.g. "product", "image").
 */
@Mapper(componentModel = "spring")
public abstract class ProductMapper {

    @Autowired protected FileUploadService      fileUploadService;
    @Autowired protected ProductImageRepository imageRepository;

    // ── Detail response ─────────────────────────────────────────────────────

    /**
     * The images list requires a DB query + per-image URL conversion.
     * We delegate to a helper method that is visible inside the java() expression.
     */
    @Mapping(
        target  = "images",
        expression = "java(toImageResponseList(product))"
    )
    public abstract ProductResponse toDetailResponse(Product product);

    /**
     * Helper called from the java() expression above.
     * Loads all images for the product and converts each one to a response DTO.
     */
    protected List<ProductImageResponse> toImageResponseList(Product product) {
        return imageRepository
                .findByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
    }

    // ── Summary response (paginated list) ────────────────────────────────────

    @Mapping(
        target = "primaryImageUrl",
        expression = "java(resolvePrimaryImageUrl(product))"
    )
    public abstract ProductSummaryResponse toSummaryResponse(Product product);

    /**
     * Helper called from the java() expression above.
     * Returns the public URL of the primary image, or null if none.
     */
    protected String resolvePrimaryImageUrl(Product product) {
        return imageRepository
                .findByProductIdAndIsPrimaryTrue(product.getId())
                .map(img -> fileUploadService.toPublicUrl(img.getImageUrl()))
                .orElse(null);
    }

    // ── Image response (per-image) ────────────────────────────────────────────

    @Mapping(
        target = "imageUrl",
        expression = "java(fileUploadService.toPublicUrl(image.getImageUrl()))"
    )
    public abstract ProductImageResponse toImageResponse(ProductImage image);
}