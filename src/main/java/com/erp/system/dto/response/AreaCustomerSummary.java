package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Per-area aggregation row for the area-wise customers endpoint.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AreaCustomerSummary {

    private String area;
    private String city;

    /** Total customers (non-deleted) in this area */
    private Long totalCustomers;

    /** Customers with isActive = true */
    private Long activeCustomers;

    /** Customers with isRegular = true */
    private Long regularCustomers;
}
