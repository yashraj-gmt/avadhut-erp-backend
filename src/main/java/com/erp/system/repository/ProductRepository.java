package com.erp.system.repository;

import com.erp.system.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductCodeIgnoreCase(String productCode);

    boolean existsByProductCodeIgnoreCaseAndIdNot(
            String productCode,
            Long id
    );

    @Query("""
        SELECT p
        FROM Product p
        WHERE p.deleted = false

        AND (
            :search IS NULL

            OR LOWER(p.name)
                LIKE CONCAT(
                    '%',
                    LOWER(CAST(:search AS string)),
                    '%'
                )

            OR LOWER(p.productCode)
                LIKE CONCAT(
                    '%',
                    LOWER(CAST(:search AS string)),
                    '%'
                )
        )

        AND (
            :isActive IS NULL
            OR p.isActive = :isActive
        )
        """)
    Page<Product> findAllWithFilters(
            @Param("search")
            String search,

            @Param("isActive")
            Boolean isActive,

            Pageable pageable
    );
}