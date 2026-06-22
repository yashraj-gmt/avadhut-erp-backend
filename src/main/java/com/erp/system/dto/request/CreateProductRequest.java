package com.erp.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Product code is required")
    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid purchase price format")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Rent price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid rent price format")
    private BigDecimal rentPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    @Size(max = 200, message = "Product by must not exceed 200 characters")
    private String productBy;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean isActive = true;
}