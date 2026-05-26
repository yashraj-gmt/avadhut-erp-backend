package com.erp.system.entity;

import com.erp.system.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "invoices",
    indexes = {
        @Index(name = "idx_inv_number",      columnList = "invoice_number"),
        @Index(name = "idx_inv_customer",    columnList = "customer_id"),
        @Index(name = "idx_inv_order",       columnList = "order_id"),
        @Index(name = "idx_inv_status",      columnList = "payment_status"),
        @Index(name = "idx_inv_due_date",    columnList = "due_date"),
        @Index(name = "idx_inv_created_at",  columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"invoiceItems", "payments"})
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable invoice number (e.g. INV-20240001).
     * Generated in the service layer.
     */
    @Column(name = "invoice_number", unique = true, nullable = false, length = 30)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Optional: invoice raised against a specific order */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "pending_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "gstn_number", length = 20)
    private String gstnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Version
    @Column(name = "version")
    private Integer version;

    // -- Relationships --

    @OneToMany(
        mappedBy = "invoice",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<InvoiceItem> invoiceItems = new ArrayList<>();

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    // -- Helper methods --

    public void addItem(InvoiceItem item) {
        item.setInvoice(this);
        invoiceItems.add(item);
    }
}
