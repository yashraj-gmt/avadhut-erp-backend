package com.erp.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateGeneratorRequest {

    @NotBlank(message = "Generator name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    // @NotBlank(message = "Generator code is required")
    @Size(max = 50, message = "Generator code must not exceed 50 characters")
    private String generatorCode;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid purchase price format")
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Party diesel rent price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid party diesel rent price format")
    private BigDecimal partyDieselRentPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "With diesel rent price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid with diesel rent price format")
    private BigDecimal withDieselRentPrice;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;

    @Min(value = 0, message = "Under service quantity cannot be negative")
    private Integer underServiceQuantity = 0;

    @Size(max = 200, message = "Product by must not exceed 200 characters")
    private String productBy;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean isActive = true;
}
