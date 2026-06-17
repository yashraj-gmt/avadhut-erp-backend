// com/erp/system/mapper/ProductMapper.java
package com.erp.system.mapper;

import com.erp.system.dto.response.ProductImageResponse;
import com.erp.system.dto.response.ProductResponse;
import com.erp.system.dto.response.ProductSummaryResponse;
import com.erp.system.entity.Product;
import com.erp.system.entity.ProductImage;
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

    @Autowired protected FileUploadService      fileUploadService;
    @Autowired protected ProductImageRepository imageRepository;

    // ── Detail response ───────────────────────────────────────────────────

    @Mapping(target = "images", ignore = true)
    public abstract ProductResponse toDetailResponse(Product product);

    @AfterMapping
    protected void enrichDetailResponse(Product p,
                                        @MappingTarget ProductResponse r) {
        List<ProductImage> imgs =
                imageRepository.findByProductIdOrderByDisplayOrderAsc(p.getId());
        r.setImages(imgs.stream().map(this::toImageResponse).toList());
    }

    // ── Summary response (list) ───────────────────────────────────────────

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