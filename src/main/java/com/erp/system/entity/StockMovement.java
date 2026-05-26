package com.erp.system.entity;

import com.erp.system.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock_movements",
    indexes = {
        @Index(name = "idx_sm_product_id",   columnList = "product_id"),
        @Index(name = "idx_sm_type",         columnList = "movement_type"),
        @Index(name = "idx_sm_created_at",   columnList = "created_at"),
        @Index(name = "idx_sm_reference_id", columnList = "reference_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class StockMovement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** Stock level recorded immediately after this movement */
    @Column(name = "stock_after_movement", nullable = false)
    private Integer stockAfterMovement;

    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /**
     * Polymorphic reference: links to order_id, invoice_id etc.
     * Combined with reference_type for disambiguation.
     */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
