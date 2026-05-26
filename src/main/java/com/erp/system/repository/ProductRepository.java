// com/erp/system/repository/ProductRepository.java
package com.erp.system.repository;

import com.erp.system.entity.Product;
import com.erp.system.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductCodeIgnoreCase(String productCode);
    boolean existsByProductCodeIgnoreCaseAndIdNot(String productCode, Long id);

    @Query("""
        SELECT DISTINCT p FROM Product p
        LEFT JOIN p.inventories i
        WHERE p.deleted = false
          AND (:search      IS NULL
               OR LOWER(p.name)        LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:categoryId  IS NULL OR p.category.id = :categoryId)
          AND (:status      IS NULL OR p.status      = :status)
          AND (:isActive    IS NULL OR p.isActive    = :isActive)
          AND (:warehouseId IS NULL OR i.warehouse.id = :warehouseId)
        """)
    Page<Product> findAllWithFilters(
            @Param("search")      String        search,
            @Param("categoryId")  Long          categoryId,
            @Param("status")      ProductStatus status,
            @Param("isActive")    Boolean       isActive,
            @Param("warehouseId") Long          warehouseId,
            Pageable pageable);
}