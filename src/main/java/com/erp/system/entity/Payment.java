package com.erp.system.entity;

import com.erp.system.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_pay_invoice_id",   columnList = "invoice_id"),
        @Index(name = "idx_pay_customer_id",  columnList = "customer_id"),
        @Index(name = "idx_pay_date",         columnList = "payment_date"),
        @Index(name = "idx_pay_collected_by", columnList = "collected_by_id"),
        @Index(name = "idx_pay_mode",         columnList = "payment_mode")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "pending_after_payment", precision = 15, scale = 2)
    private BigDecimal pendingAfterPayment;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private PaymentMode paymentMode;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /** Cheque number / UPI reference / bank transaction ref */
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collected_by_id")
    private User collectedBy;
}
