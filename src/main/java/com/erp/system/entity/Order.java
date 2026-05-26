package com.erp.system.entity;

import com.erp.system.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
     * Human-readable order number (e.g. ORD-20240001).
     * Generated in the service layer before persisting.
     */
    @Column(name = "order_number", unique = true, nullable = false, length = 30)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus = OrderStatus.PENDING;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

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

    // -- Helper methods --

    public void addItem(OrderItem item) {
        item.setOrder(this);
        orderItems.add(item);
    }

    public void removeItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}
