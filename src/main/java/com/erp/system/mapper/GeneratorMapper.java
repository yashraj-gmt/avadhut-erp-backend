package com.erp.system.mapper;

import com.erp.system.dto.response.GeneratorResponse;
import com.erp.system.entity.Generator;
import com.erp.system.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps Generator entity → GeneratorResponse DTO.
 *
 * Implemented as a Spring @Component to guarantee bean availability
 * regardless of IDE or build-tool annotation processing configuration.
 */
@Component
@RequiredArgsConstructor
public class GeneratorMapper {

    private final FileUploadService fileUploadService;

    public GeneratorResponse toResponse(Generator generator) {
        if (generator == null) {
            return null;
        }

        return GeneratorResponse.builder()
                .id(generator.getId())
                .name(generator.getName())
                .generatorCode(generator.getGeneratorCode())
                .purchasePrice(generator.getPurchasePrice())
                .partyDieselRentPrice(generator.getPartyDieselRentPrice())
                .withDieselRentPrice(generator.getWithDieselRentPrice())
                .stockQuantity(generator.getStockQuantity())
                .underServiceQuantity(generator.getUnderServiceQuantity())
                .productBy(generator.getProductBy())
                .imageUrl(fileUploadService != null ? fileUploadService.toPublicUrl(generator.getImageUrl()) : generator.getImageUrl())
                .description(generator.getDescription())
                .isActive(generator.getIsActive())
                .createdAt(generator.getCreatedAt())
                .updatedAt(generator.getUpdatedAt())
                .build();
    }
}
