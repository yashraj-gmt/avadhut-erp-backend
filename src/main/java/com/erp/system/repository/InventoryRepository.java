// com/erp/system/repository/InventoryRepository.java
package com.erp.system.repository;

import com.erp.system.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    List<Inventory> findByProductId(Long productId);

    Page<Inventory> findByWarehouseId(Long warehouseId, Pageable pageable);

    Optional<Inventory> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    long countByProductId(Long productId);

    @Query("""
        SELECT i FROM Inventory i
        WHERE i.deleted = false
          AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
          AND (:productId   IS NULL OR i.product.id   = :productId)
          AND (:search      IS NULL
               OR LOWER(i.product.name)        LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(i.product.productCode) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Inventory> findAllWithFilters(
            @Param("warehouseId") Long     warehouseId,
            @Param("productId")   Long     productId,
            @Param("search")      String   search,
            Pageable pageable);

    /**
     * Low-stock: rows where stockQuantity <= stockAlert (alert % <= 100).
     */
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.deleted      = false
          AND i.stockAlert   IS NOT NULL
          AND i.stockAlert   > 0
          AND i.stockQuantity <= i.stockAlert
          AND (:warehouseId  IS NULL OR i.warehouse.id = :warehouseId)
        """)
    Page<Inventory> findLowStockItems(
            @Param("warehouseId") Long warehouseId,
            Pageable pageable);

    /**
     * Items whose alert-percentage is at or below the given threshold.
     * Formula: (stockQuantity / stockAlert) * 100 <= threshold
     */
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.deleted     = false
          AND i.stockAlert  IS NOT NULL
          AND i.stockAlert  > 0
          AND (CAST(i.stockQuantity AS double) / CAST(i.stockAlert AS double)) * 100 <= :threshold
        """)
    Page<Inventory> findItemsBelowAlertThreshold(
            @Param("threshold") Double threshold,
            Pageable pageable);
}