package com.erp.system.dto.response;

import com.erp.system.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Compact invoice summary embedded inside CustomerProfileResponse.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InvoiceSummaryDto {

    private Long          id;
    private String        invoiceNumber;
    private PaymentStatus paymentStatus;
    private BigDecimal    finalAmount;
    private BigDecimal    pendingAmount;
    private LocalDate     dueDate;
    private LocalDate     invoiceDate;
    private LocalDateTime createdAt;
}
