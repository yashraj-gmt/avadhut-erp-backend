package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Individual overdue invoice detail embedded inside PendingPaymentResponse.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OverdueInvoiceInfo {

    private Long       invoiceId;
    private String     invoiceNumber;
    private BigDecimal pendingAmount;
    private LocalDate  dueDate;

    /** Number of days overdue (positive = overdue, 0 = due today). */
    private Long overdueDays;
}
