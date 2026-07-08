package com.erp.system.dto.request;

import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import lombok.*;

import java.time.LocalDate;

/**
 * Query parameter wrapper for customer search &amp; filter operations.
 * Used by both the list-all and search endpoints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerFilterRequest {

    /** Searches across name, mobile, and area (case-insensitive LIKE). */
    private String search;

    private CustomerType customerType;

    private CustomerStatus customerStatus;

    /** Filter by isActive flag */
    private Boolean isActive;

    /** Filter by isRegular flag */
    private Boolean isRegular;

    /** Filter by exact area name */
    private String area;

    /** Filter by exact city name */
    private String city;

    /** Inclusive lower bound for dateJoined filter */
    private LocalDate dateJoinedFrom;

    /** Inclusive upper bound for dateJoined filter */
    private LocalDate dateJoinedTo;
}
