// com/erp/system/repository/WarehouseRepository.java
package com.erp.system.repository;

import com.erp.system.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("""
        SELECT w FROM Warehouse w
        WHERE w.deleted = false
          AND (:search   IS NULL OR LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%'))
                                 OR LOWER(w.code) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR w.isActive = :isActive)
        """)
    Page<Warehouse> findAllWithFilters(
            @Param("search")   String  search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("""
        SELECT COUNT(i) FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
          AND i.deleted = false
        """)
    long countProductsByWarehouseId(@Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COALESCE(SUM(i.stockQuantity), 0) FROM Inventory i
        WHERE i.warehouse.id = :warehouseId
          AND i.deleted = false
        """)
    long sumStockByWarehouseId(@Param("warehouseId") Long warehouseId);
}