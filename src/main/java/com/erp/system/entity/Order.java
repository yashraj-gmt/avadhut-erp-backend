package com.erp.system.entity;

import com.erp.system.enums.OrderStatus;
import com.erp.system.enums.BillingStatus;
import com.erp.system.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_number",      columnList = "order_number"),
        @Index(name = "idx_orders_customer",    columnList = "customer_id"),
        @Index(name = "idx_orders_status",      columnList = "order_status"),
        @Index(name = "idx_orders_delivery_dt", columnList = "delivery_date"),
        @Index(name = "idx_orders_assigned_to", columnList = "assigned_to_id"),
        @Index(name = "idx_orders_created_at",  columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"orderItems", "invoices"})
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable order number in GO{year}{seq} format (e.g. GO20261, GO20262).
     * Generated in the service layer before persisting. Must be unique.
     */
    @Column(name = "order_number", unique = true, nullable = false, length = 30)
    private String orderNumber;

    @Column(name = "bill_number", unique = true, length = 30)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_status", nullable = false, length = 20)
    private BillingStatus billingStatus = BillingStatus.PENDING;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "function_date_from")
    private LocalDate functionDateFrom;

    @Column(name = "function_date_to")
    private LocalDate functionDateTo;

    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Optional alternate contact number for the client */
    @Column(name = "alternate_mobile", length = 20)
    private String alternateMobile;

    /** Address of the site */
    @Column(name = "site_address", columnDefinition = "TEXT")
    private String siteAddress;

    /** Google Maps or search link for the site location */
    @Column(name = "site_address_link", columnDefinition = "TEXT")
    private String siteAddressLink;

    /** Mobile number of the assigned operator */
    @Column(name = "operator_mobile", length = 20)
    private String operatorMobile;

    /** Name of the assigned operator */
    @Column(name = "operator_name", length = 150)
    private String operatorName;

    /** Whether cable is required for this order */
    @Column(name = "cable_required")
    private Boolean cableRequired = false;

    /** True = diesel provided with owner/company; False = party arranges diesel */
    @Column(name = "with_diesel")
    private Boolean withDiesel = true;

    /** Date by which payment is expected. Defaults to billing creation date + 7 days. Null until billing is first saved. */
    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    /** Tracks whether the client has paid. Defaults to PENDING and never auto-changed by existing billing flows. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    /** Cumulative amount paid by customer for this order (supports partial payments) */
    @Column(name = "paid_amount", precision = 15, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /** Remaining pending balance = finalAmount - paidAmount */
    @Column(name = "pending_amount", precision = 15, scale = 2)
    private BigDecimal pendingAmount = BigDecimal.ZERO;

    /** Date when the payment was completed in full (null if pending/partial) */
    @Column(name = "payment_completion_date")
    private LocalDate paymentCompletionDate;

    /**
     * Optional miscellaneous charges stored as JSON array string.
     * Format: [{"name":"Catering","amount":500.00}, ...]
     * Null/empty means no other charges.
     */
    @Column(name = "other_charges", columnDefinition = "TEXT")
    private String otherCharges;

    /**
     * Timestamp when the generators for this order were marked as physically returned.
     * Once set, these generator units are excluded from booked-stock calculations,
     * allowing same-day re-booking. Billing workflow is unaffected.
     */
    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    /** Employee/user responsible for this order */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /** Optimistic locking to prevent concurrent modification */
    @Version
    @Column(name = "version")
    private Integer version;

    // -- Relationships --

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();

    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        fetch = FetchType.LAZY
    )
    @OrderBy("paymentDate DESC, createdAt DESC")
    private List<Payment> payments = new ArrayList<>();

    // -- Helper methods --

    public BigDecimal getPaidAmount() {
        return paidAmount != null ? paidAmount : BigDecimal.ZERO;
    }

    public BigDecimal getPendingAmount() {
        if (pendingAmount != null) return pendingAmount;
        if (finalAmount != null) return finalAmount.subtract(getPaidAmount()).max(BigDecimal.ZERO);
        return BigDecimal.ZERO;
    }

    public void addItem(OrderItem item) {
        item.setOrder(this);
        orderItems.add(item);
    }

    public void removeItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}
