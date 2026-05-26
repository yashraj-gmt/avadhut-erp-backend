package com.erp.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * All fields optional — only non-null values are applied (PATCH semantics).
 */
@Getter
@Setter
public class UpdateWarehouseRequest {

    @Size(min = 1, max = 150)
    private String name;

    @Size(min = 1, max = 30)
    @Pattern(regexp = "^[A-Z0-9_-]+$",
            message = "Code must contain only uppercase letters, digits, hyphens, or underscores")
    private String code;

    @Size(max = 255)
    private String location;

    @Size(max = 2000)
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 10)
    @Pattern(regexp = "^[0-9]{0,10}$", message = "Pincode must be numeric")
    private String pincode;

    @Size(max = 100)
    private String contactPerson;

    @Size(max = 20)
    @Pattern(regexp = "^[+0-9\\s-]{0,20}$", message = "Invalid phone format")
    private String contactPhone;

    @Min(value = 0)
    private Integer totalCapacity;

    private Boolean isActive;
}