// com/erp/system/repository/ProductImageRepository.java
package com.erp.system.repository;

import com.erp.system.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    Optional<ProductImage> findByProductIdAndIsPrimaryTrue(Long productId);

    long countByProductId(Long productId);

    @Query("""
        SELECT img FROM ProductImage img
        WHERE img.id IN :ids AND img.product.id = :productId AND img.deleted = false
        """)
    List<ProductImage> findByIdsAndProductId(
            @Param("ids")       List<Long> ids,
            @Param("productId") Long       productId);

    @Modifying
    @Query("""
        UPDATE ProductImage img SET img.isPrimary = false
        WHERE img.product.id = :productId AND img.deleted = false
        """)
    void clearPrimaryForProduct(@Param("productId") Long productId);
}