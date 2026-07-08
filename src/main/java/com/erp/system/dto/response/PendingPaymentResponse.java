package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Pending-payment view row: one row per customer who has outstanding invoices.
 * Sortable by totalDueAmount or maxOverdueDays.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PendingPaymentResponse {

    private Long   customerId;
    private String customerName;
    private String mobile;
    private String area;
    private String city;

    /** Sum of pendingAmount across all unpaid/partially-paid invoices. */
    private BigDecimal totalDueAmount;

    /** Date of the most recent payment received from this customer. */
    private LocalDate lastPaymentDate;

    /** Maximum overdueDays across this customer's overdue invoices. */
    private Long maxOverdueDays;

    /** Individual invoice breakdown for drill-down. */
    private List<OverdueInvoiceInfo> overdueInvoices;
}
