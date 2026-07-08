package com.erp.system.dto.response;

import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rich profile response: basic info + linked orders/invoices summary
 * + total business value + outstanding dues.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfileResponse {

    // -- Basic Info --
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

    // -- Linked summaries --
    private List<OrderSummaryDto>   recentOrders;
    private List<InvoiceSummaryDto> recentInvoices;

    // -- Aggregates --
    private Long       totalOrders;
    private Long       totalInvoices;

    /** Sum of finalAmount across all invoices (gross business value). */
    private BigDecimal totalBusinessValue;

    /** Sum of paidAmount across all invoices. */
    private BigDecimal totalPaidAmount;

    /** totalBusinessValue - totalPaidAmount */
    private BigDecimal outstandingDues;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
