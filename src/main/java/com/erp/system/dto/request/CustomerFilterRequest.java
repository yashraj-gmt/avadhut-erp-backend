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

    /** Searches across name, firmName, mobile/alternate/telephone, and location (address/area/city) */
    private String search;

    /** Filter specifically by firm/company name */
    private String firmName;

    /** Filter specifically by mobile/telephone number */
    private String mobile;

    /** Filter specifically by location (address, area, city) */
    private String location;

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
