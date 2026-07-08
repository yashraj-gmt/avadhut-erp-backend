package com.erp.system.dto.response;

import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Full customer detail response (used for get-by-id and create/update).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerResponse {

    private Long           id;
    private String         name;
    private String         mobile;
    private String         alternateMobile;
    private String         email;
    private String         address;
    private String         city;
    private String         area;
    private String         pincode;
    private CustomerType   customerType;
    private CustomerStatus customerStatus;
    private Boolean        isActive;
    private Boolean        isRegular;
    private LocalDate      regularSince;
    private LocalDate      dateJoined;
    private String         notes;

    // -- Computed aggregates (populated by service) --

    /** Count of non-deleted orders */
    private Long       totalOrders;

    /** Sum of finalAmount across all invoices */
    private BigDecimal totalInvoiceAmount;

    /** Sum of paidAmount across all invoices */
    private BigDecimal totalPaidAmount;

    /** Remaining dues = totalInvoiceAmount - totalPaidAmount */
    private BigDecimal outstandingDues;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
