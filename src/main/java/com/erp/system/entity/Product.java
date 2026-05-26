package com.erp.system.entity;

import com.erp.system.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * UPDATED from original — additions:
 *  • status        (DRAFT / PUBLISHED)
 *  • qrCodeImage   (relative path to QR image)
 *  • images        (OneToMany → ProductImage)
 *  • inventories   (OneToMany → Inventory)
 */
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_code",     columnList = "product_code"),
                @Index(name = "idx_product_category", columnList = "category_id"),
                @Index(name = "idx_product_stock",    columnList = "current_stock"),
                @Index(name = "idx_product_active",   columnList = "is_active"),
                @Index(name = "idx_product_status",   columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"stockMovements", "orderItems", "invoiceItems", "images", "inventories"})
@SQLRestriction("deleted = false")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "product_code", unique = true, nullable = false, length = 50)
    private String productCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    /** Unit of measure: PCS, KG, LITRE, BOX, etc. */
    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "selling_price", precision = 15, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "rent_price", precision = 15, scale = 2)
    private BigDecimal rentPrice;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock = 0;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "gst_percent", precision = 5, scale = 2)
    private BigDecimal gstPercent;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // ── NEW FIELDS ────────────────────────────────────────────────────────

    /** Draft / Published workflow */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status = ProductStatus.DRAFT;

    /** Relative path to the single QR code image, e.g. products/qr/uuid.png */
    @Column(name = "qr_code_image", length = 500)
    private String qrCodeImage;

    // ── Relationships ─────────────────────────────────────────────────────

    /** Multiple product gallery images */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    /** Inventory records across warehouses */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();

    /** (existing) */
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<StockMovement> stockMovements = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<InvoiceItem> invoiceItems = new ArrayList<>();
}