package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "order_items",
    indexes = {
        @Index(name = "idx_oi_order_id",   columnList = "order_id"),
        @Index(name = "idx_oi_product_id", columnList = "product_id"),
        @Index(name = "idx_oi_generator_id", columnList = "generator_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generator_id")
    private Generator generator;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;   // snapshot at order time of either product or generator name

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "rate", nullable = false, precision = 15, scale = 2)
    private BigDecimal rate;

    @Column(name = "diesel_rate", precision = 15, scale = 2)
    private BigDecimal dieselRate;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_percent", precision = 5, scale = 2)
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "notes", length = 255)
    private String notes;

    /**
     * Selected cable size for this generator line item.
     * Static values: "10", "16", "25", "35", "50", "70", "95", "120",
     * "150", "185", "240", "300", or "Earth Rod".
     * Null if no cable selected.
     */
    @Column(name = "cable_size", length = 30)
    private String cableSize;

    /** Billing date for this specific generator row (may differ per day in multi-day orders) */
    @Column(name = "billing_date")
    private LocalDate billingDate;

    @Column(name = "start_time")
    private java.time.LocalTime startTime;

    @Column(name = "end_time")
    private java.time.LocalTime endTime;

    @Column(name = "duration", precision = 5, scale = 2)
    private java.math.BigDecimal duration;

    @OneToMany(
        mappedBy = "orderItem",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<OrderItemDieselEntry> dieselEntries = new ArrayList<>();

    public void addDieselEntry(OrderItemDieselEntry entry) {
        entry.setOrderItem(this);
        dieselEntries.add(entry);
    }
}
