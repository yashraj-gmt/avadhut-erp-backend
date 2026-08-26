package com.erp.system.dto.request;

import com.erp.system.enums.CustomerStatus;
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

    @Size(max = 150, message = "Firm name must not exceed 150 characters.")
    private String firmName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile must be a valid 10-digit Indian number.")
    private String mobile;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Alternate mobile must be a valid 10-digit Indian number.")
    private String alternateMobile;

    @Email(message = "Email must be a valid address.")
    @Size(max = 100, message = "Email must not exceed 100 characters.")
    private String email;

    /** Site / installation address */
    @Size(max = 500, message = "Address must not exceed 500 characters.")
    private String address;

    /** Google Maps or location link for site address */
    @Size(max = 500, message = "Location link must not exceed 500 characters.")
    private String addressLocationLink;

    /** Remarks / special instructions */
    private String remarks;

    private String notes;

    /** Only ACTIVE / INACTIVE allowed from UI (BLOCKED preserved in DB only) */
    private CustomerStatus customerStatus;

    private Boolean isActive;

    /** Manual toggle to mark as regular */
    private Boolean isRegular;

    private LocalDate dateJoined;
}
