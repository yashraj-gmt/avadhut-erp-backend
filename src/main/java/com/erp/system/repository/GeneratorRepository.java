package com.erp.system.repository;

import com.erp.system.entity.Generator;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
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

    boolean existsBySerialNumberIgnoreCase(String serialNumber);

    boolean existsBySerialNumberIgnoreCaseAndIdNot(String serialNumber, Long id);

    @Query("""
        SELECT g FROM Generator g
        WHERE g.deleted = false
          AND (
              COALESCE(:search, '') = ''
              OR LOWER(g.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(g.generatorCode) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(COALESCE(g.brand, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
              OR LOWER(COALESCE(g.serialNumber, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
          )
          AND (:currentStatus IS NULL OR g.currentStatus = :currentStatus)
          AND (:fuelType IS NULL OR g.fuelType = :fuelType)
          AND (:isActive IS NULL OR g.isActive = :isActive)
        ORDER BY g.createdAt DESC
        """)
    Page<Generator> findAllWithFilters(
            @Param("search") String search,
            @Param("currentStatus") GeneratorStatus currentStatus,
            @Param("fuelType") GeneratorFuelType fuelType,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    long countByCurrentStatusAndDeletedFalse(GeneratorStatus currentStatus);
}
