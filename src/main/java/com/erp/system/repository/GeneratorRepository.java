package com.erp.system.repository;

import com.erp.system.entity.Generator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.erp.system.entity.OrderItem;
import java.time.LocalDate;
import java.util.List;

public interface GeneratorRepository extends JpaRepository<Generator, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByGeneratorCodeIgnoreCase(String generatorCode);

    boolean existsByGeneratorCodeIgnoreCaseAndIdNot(String generatorCode, Long id);

    java.util.Optional<Generator> findByGeneratorCodeIgnoreCase(String generatorCode);

    @Query("""
        SELECT g FROM Generator g
        WHERE g.deleted = false
          AND (
              COALESCE(:search, '') = ''
              OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(g.generatorCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          )
          AND (:isActive IS NULL OR g.isActive = :isActive)
        ORDER BY g.createdAt DESC
        """)
    Page<Generator> findAllWithFilters(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    @Query("SELECT MAX(g.id) FROM Generator g")
    Long getMaxId();

    @Query("""
        SELECT oi
        FROM   OrderItem oi
        JOIN   oi.order o
        WHERE  oi.generator IS NOT NULL
        AND    o.deleted = false
        AND    o.orderStatus != com.erp.system.enums.OrderStatus.CANCELLED
        AND    o.functionDateFrom <= :endDate
        AND    o.functionDateTo >= :startDate
        AND    (:excludeOrderId IS NULL OR o.id != :excludeOrderId)
        """)
    List<OrderItem> findAllOverlappingBookings(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeOrderId") Long excludeOrderId
    );
}
