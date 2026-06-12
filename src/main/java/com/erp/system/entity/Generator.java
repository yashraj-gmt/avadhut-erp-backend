package com.erp.system.entity;

import com.erp.system.enums.GeneratorCondition;
import com.erp.system.enums.GeneratorFuelType;
import com.erp.system.enums.GeneratorStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "generators",
        indexes = {
                @Index(name = "idx_generator_code",   columnList = "generator_code"),
                @Index(name = "idx_generator_status", columnList = "current_status"),
                @Index(name = "idx_generator_active", columnList = "is_active"),
                @Index(name = "idx_generator_fuel",   columnList = "fuel_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString
@SQLRestriction("deleted = false")
public class Generator extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "generator_code", unique = true, nullable = false, length = 50)
    private String generatorCode;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "model", length = 100)
    private String model;

    @Column(name = "serial_number", unique = true, length = 100)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private GeneratorFuelType fuelType;

    @Column(name = "rated_power_kva", precision = 10, scale = 2)
    private BigDecimal ratedPowerKva;

    @Column(name = "rated_power_kw", precision = 10, scale = 2)
    private BigDecimal ratedPowerKw;

    @Column(name = "voltage", precision = 10, scale = 2)
    private BigDecimal voltage;

    @Column(name = "frequency", precision = 6, scale = 2)
    private BigDecimal frequency;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "rent_price_per_day", precision = 15, scale = 2)
    private BigDecimal rentPricePerDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 20)
    private GeneratorStatus currentStatus = GeneratorStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "generator_condition", length = 20)
    private GeneratorCondition condition;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "hours_run")
    private Integer hoursRun = 0;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_due")
    private LocalDate nextServiceDue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
