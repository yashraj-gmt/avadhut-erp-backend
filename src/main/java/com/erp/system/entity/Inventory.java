package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "inventory",
        indexes = {
                @Index(name = "idx_inv_product_id",   columnList = "product_id"),
                @Index(name = "idx_inv_warehouse_id", columnList = "warehouse_id"),
                @Index(name = "idx_inv_stock_qty",    columnList = "stock_quantity")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name        = "uk_inv_product_warehouse",
                        columnNames = {"product_id", "warehouse_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@SQLRestriction("deleted = false")
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    /** Current stock quantity stored in this warehouse for this product */
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    /**
     * Alert threshold — when stockQuantity hits or drops below this,
     * the item is considered "low stock".
     * Alert % formula: (stockQuantity / stockAlert) * 100
     *   < 100  → low stock
     *   >= 100 → sufficient stock
     */
    @Column(name = "stock_alert")
    private Integer stockAlert;

    /**
     * Maximum quantity this warehouse can hold for this product.
     * Utilisation % = (stockQuantity / warehouseCapacity) * 100
     */
    @Column(name = "warehouse_capacity")
    private Integer warehouseCapacity;

    // ── Transient Calculated Helpers ──────────────────────────────────────

    /**
     * Alert percentage = (currentStock / stockAlert) * 100
     * A value < 100 means stock is BELOW the alert threshold → LOW STOCK.
     */
    @Transient
    public Double getStockAlertPercentage() {
        if (stockAlert == null || stockAlert == 0) return null;
        return Math.round(
                (stockQuantity.doubleValue() / stockAlert.doubleValue()) * 100.0 * 100.0
        ) / 100.0;
    }

    /** Returns true when stockQuantity ≤ stockAlert. */
    @Transient
    public Boolean isLowStock() {
        Double pct = getStockAlertPercentage();
        return pct != null && pct <= 100.0;
    }

    /** Capacity utilisation percentage: (stockQuantity / warehouseCapacity) * 100 */
    @Transient
    public Double getCapacityUtilisationPercentage() {
        if (warehouseCapacity == null || warehouseCapacity == 0) return null;
        return Math.round(
                (stockQuantity.doubleValue() / warehouseCapacity.doubleValue()) * 100.0 * 100.0
        ) / 100.0;
    }
}