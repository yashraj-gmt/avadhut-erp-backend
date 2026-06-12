package com.erp.system.dto.request;

import com.erp.system.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sent as the "data" part in a PATCH multipart/form-data request.
 * Only non-null fields are applied (true PATCH semantics).
 *
 * To remove existing images, pass their IDs in removeImageIds.
 * To add new images, include new files in the "images" multipart part.
 * To replace the QR code, include a new file in the "qrCode" part.
 */
@Getter
@Setter
public class UpdateProductRequest {

    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    private String name;

    @Size(min = 1, max = 50, message = "Product code must be between 1 and 50 characters")
    private String productCode;

    private Long categoryId;

    private BigDecimal weight;

    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal rentPrice;

    @Min(value = 0)
    private Integer minimumStock;

    @Size(max = 20)
    private String hsnCode;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal gstPercent;

    @Size(max = 5000)
    private String description;

    private Boolean isActive;

    @Size(max = 200, message = "Product by must not exceed 200 characters")
    private String productBy;

    /** Change product status (DRAFT ↔ PUBLISHED) */
    private ProductStatus status;

    // ── Image management ──────────────────────────────────────────────────

    /** IDs of existing product images to remove */
    private List<Long> removeImageIds;

    /** ID of image to mark as primary (must already belong to this product) */
    private Long setPrimaryImageId;

    // ── Inventory / Warehouse fields ──────────────────────────────────────

    private Long warehouseId;

    @Min(value = 0)
    private Integer stockQuantity;

    @Min(value = 0)
    private Integer stockAlert;

    @Min(value = 0)
    private Integer warehouseCapacity;
}