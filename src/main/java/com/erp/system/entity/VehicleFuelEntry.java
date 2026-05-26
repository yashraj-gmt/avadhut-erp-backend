package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "vehicle_fuel_entries",
    indexes = {
        @Index(name = "idx_vfe_vehicle_id", columnList = "vehicle_id"),
        @Index(name = "idx_vfe_date",       columnList = "entry_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class VehicleFuelEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(name = "liters", nullable = false, precision = 8, scale = 2)
    private BigDecimal liters;

    @Column(name = "rate_per_liter", precision = 8, scale = 2)
    private BigDecimal ratePerLiter;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "odometer_reading")
    private Integer odometerReading;

    @Column(name = "fuel_station", length = 200)
    private String fuelStation;

    @Column(name = "notes", length = 255)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    private User addedBy;
}
