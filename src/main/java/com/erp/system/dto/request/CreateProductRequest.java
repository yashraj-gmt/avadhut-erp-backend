package com.erp.system.dto.request;

import com.erp.system.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Sent as the "data" part in a multipart/form-data request.
 * Example (Postman / curl):
 *
 *   POST /api/inventory/products
 *   Content-Type: multipart/form-data
 *
 *   Part "data"    → application/json  (this DTO)
 *   Part "images"  → image files (multiple)
 *   Part "qrCode"  → single QR image file
 */
@Getter
@Setter
public class CreateProductRequest {

    // ── Core product fields ───────────────────────────────────────────────

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    private Long categoryId;

    @Size(max = 20, message = "Unit must not exceed 20 characters")
    private String unit;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid purchase price format")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid selling price format")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Rent price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid rent price format")
    private BigDecimal rentPrice;

    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock = 0;

    @Size(max = 20, message = "HSN code must not exceed 20 characters")
    private String hsnCode;

    @DecimalMin(value = "0.0", message = "GST percent cannot be negative")
    @DecimalMax(value = "100.0", message = "GST percent cannot exceed 100")
    private BigDecimal gstPercent;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean isActive = true;

    // ── Draft / Publish ───────────────────────────────────────────────────

    /**
     * DRAFT (default) or PUBLISHED.
     * Leaving null defaults to DRAFT.
     */
    private ProductStatus status = ProductStatus.DRAFT;

    // ── Inventory / Warehouse fields ──────────────────────────────────────

    /** Optional: link to a warehouse and create an inventory record */
    private Long warehouseId;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    /** Low-stock alert threshold. Alert % = (stockQuantity / stockAlert) * 100 */
    @Min(value = 0, message = "Stock alert cannot be negative")
    private Integer stockAlert;

    /** Max units this warehouse can hold for this product */
    @Min(value = 0, message = "Warehouse capacity cannot be negative")
    private Integer warehouseCapacity;
}