package com.erp.system.dto.request;

import com.erp.system.enums.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordPaymentRequest {

    @NotNull(message = "Payment amount is required.")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero.")
    private BigDecimal amount;

    @Builder.Default
    private PaymentMode paymentMode = PaymentMode.CASH;

    private LocalDate paymentDate;

    private String transactionReference;

    private String notes;
}
