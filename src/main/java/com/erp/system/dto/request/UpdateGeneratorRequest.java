package com.erp.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateGeneratorRequest {

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 50, message = "Generator code must not exceed 50 characters")
    private String generatorCode;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal purchasePrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal partyDieselRentPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 13, fraction = 2)
    private BigDecimal withDieselRentPrice;

    @Min(value = 0)
    private Integer stockQuantity;

    @Min(value = 0, message = "Under service quantity cannot be negative")
    private Integer underServiceQuantity;

    @Size(max = 200, message = "Product by must not exceed 200 characters")
    private String productBy;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean isActive;
}
