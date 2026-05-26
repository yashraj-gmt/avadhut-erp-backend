package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_pi_product_id", columnList = "product_id"),
                @Index(name = "idx_pi_primary",    columnList = "is_primary"),
                @Index(name = "idx_pi_order",      columnList = "display_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@SQLRestriction("deleted = false")
public class ProductImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Relative path stored in DB, e.g. products/images/uuid.jpg */
    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}