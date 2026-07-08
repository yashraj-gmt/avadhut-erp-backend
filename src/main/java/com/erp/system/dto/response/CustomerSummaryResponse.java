package com.erp.system.dto.response;

import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lightweight response used in list/search result pages.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerSummaryResponse {

    private Long           id;
    private String         name;
    private String         mobile;
    private String         area;
    private String         city;
    private CustomerType   customerType;
    private CustomerStatus customerStatus;
    private Boolean        isActive;
    private Boolean        isRegular;
    private LocalDate      dateJoined;

    /** Total non-deleted orders count — populated by service layer */
    private Long  totalOrders;

    /** Total number of times products were booked/ordered */
    private Long  totalBookings;

    private LocalDateTime createdAt;
}
