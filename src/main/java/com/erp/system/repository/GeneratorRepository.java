package com.erp.system.repository;

import com.erp.system.entity.Generator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GeneratorRepository extends JpaRepository<Generator, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByGeneratorCodeIgnoreCase(String generatorCode);

    boolean existsByGeneratorCodeIgnoreCaseAndIdNot(String generatorCode, Long id);

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
}
