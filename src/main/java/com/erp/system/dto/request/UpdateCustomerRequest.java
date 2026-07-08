package com.erp.system.dto.request;

import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * All fields are optional — only non-null values are applied (patch semantics).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCustomerRequest {

    @Size(max = 100, message = "Name must not exceed 100 characters.")
    private String name;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile must be a valid 10-digit Indian number.")
    private String mobile;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Alternate mobile must be a valid 10-digit Indian number.")
    private String alternateMobile;

    @Email(message = "Email must be a valid address.")
    @Size(max = 100, message = "Email must not exceed 100 characters.")
    private String email;

    @Size(max = 500, message = "Address must not exceed 500 characters.")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String city;

    @Size(max = 100, message = "Area must not exceed 100 characters.")
    private String area;

    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be a 6-digit number.")
    private String pincode;

    private CustomerType customerType;

    private CustomerStatus customerStatus;

    private Boolean isActive;

    private String notes;

    private LocalDate dateJoined;
}
