package com.erp.system.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @NotBlank(message = "Warehouse code is required")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$",
            message = "Code must contain only uppercase letters, digits, hyphens, or underscores")
    private String code;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Size(max = 2000, message = "Address must not exceed 2000 characters")
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 10, message = "Pincode must not exceed 10 characters")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "Pincode must be numeric")
    private String pincode;

    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    private String contactPerson;

    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    @Pattern(regexp = "^[+0-9\\s-]{0,20}$", message = "Invalid phone format")
    private String contactPhone;

    @Min(value = 0, message = "Total capacity cannot be negative")
    private Integer totalCapacity;

    private Boolean isActive = true;
}