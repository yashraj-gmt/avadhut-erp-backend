package com.erp.system.dto.response;

import com.erp.system.enums.PaymentMode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentSummaryDto {

    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private BigDecimal pendingAfterPayment;
    private PaymentMode paymentMode;
    private LocalDate paymentDate;
    private String transactionReference;
    private String notes;
    private String collectedByName;
    private LocalDateTime createdAt;
}
