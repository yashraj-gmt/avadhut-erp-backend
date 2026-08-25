package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(
        name = "generators",
        indexes = {
                @Index(name = "idx_generator_code",   columnList = "generator_code"),
                @Index(name = "idx_generator_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
@SQLRestriction("deleted = false")
public class Generator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "generator_code", unique = true, length = 50)
    private String generatorCode;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "party_diesel_rent_price", precision = 15, scale = 2)
    private BigDecimal partyDieselRentPrice;

    @Column(name = "with_diesel_rent_price", precision = 15, scale = 2)
    private BigDecimal withDieselRentPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "product_by", length = 200)
    private String productBy;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Number of units currently under service / not working.
     * These units are excluded from the bookable available stock.
     * Bookable = stockQuantity - underServiceQuantity - currentlyBooked
     */
    @Column(name = "under_service_quantity", nullable = false)
    private Integer underServiceQuantity = 0;
}
