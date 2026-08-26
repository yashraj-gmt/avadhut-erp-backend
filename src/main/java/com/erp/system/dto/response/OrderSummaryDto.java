package com.erp.system.dto.response;

import com.erp.system.enums.BillingStatus;
import com.erp.system.enums.OrderStatus;
import com.erp.system.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rich order summary embedded inside CustomerProfileResponse with full payment tracking.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderSummaryDto {

    private Long          id;
    private String        orderNumber;
    private String        billNumber;
    private OrderStatus   orderStatus;
    private BillingStatus billingStatus;

    private LocalDate     deliveryDate;
    private LocalDate     functionDateFrom;
    private LocalDate     functionDateTo;
    private String        functionDate;

    private String        siteAddress;
    private String        siteAddressLink;

    private BigDecimal    subtotal;
    private BigDecimal    discountAmount;
    private BigDecimal    taxAmount;
    private BigDecimal    finalAmount;
    private BigDecimal    paidAmount;
    private BigDecimal    pendingAmount;

    private PaymentStatus paymentStatus;
    private LocalDate     paymentCompletionDate;
    private LocalDate     paymentDueDate;

    private List<PaymentSummaryDto> payments;

    private LocalDateTime createdAt;
}
