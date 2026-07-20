package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerHistoryEntry {

    /** Discriminator: ORDER | INVOICE | PAYMENT */
    private String     type;

    /** The PK of the source entity (orderId / invoiceId / paymentId). */
    private Long        referenceId;

    /** Human-readable reference (orderNumber / invoiceNumber / "Payment"). */
    private String     referenceNumber;

    /** Financial amount: finalAmount for orders/invoices, amount for payments. */
    private BigDecimal amount;

    /** Status string: order status / payment status / payment mode. */
    private String     status;

    /** The event timestamp used for chronological ordering. */
    private LocalDateTime occurredAt;
}
