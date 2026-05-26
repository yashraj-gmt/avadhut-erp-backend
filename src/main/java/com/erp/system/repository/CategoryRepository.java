// com/erp/system/repository/CategoryRepository.java
package com.erp.system.repository;

import com.erp.system.entity.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<ProductCategory, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
        SELECT c FROM ProductCategory c
        WHERE c.deleted = false
          AND (:search   IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:isActive IS NULL OR c.isActive = :isActive)
        """)
    Page<ProductCategory> findAllWithFilters(
            @Param("search")   String  search,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    @Query("""
        SELECT COUNT(p) FROM Product p
        WHERE p.category.id = :categoryId
          AND p.isActive     = true
          AND p.deleted      = false
        """)
    long countActiveProductsByCategoryId(@Param("categoryId") Long categoryId);
}