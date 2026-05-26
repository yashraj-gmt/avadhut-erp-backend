package com.erp.system.entity;

import com.erp.system.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "vehicles",
    indexes = {
        @Index(name = "idx_veh_number",      columnList = "vehicle_number"),
        @Index(name = "idx_veh_type",        columnList = "vehicle_type"),
        @Index(name = "idx_veh_driver",      columnList = "driver_id"),
        @Index(name = "idx_veh_active",      columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"fuelEntries", "maintenanceRecords"})
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_number", unique = true, nullable = false, length = 20)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "make_model", length = 100)
    private String makeModel;

    @Column(name = "year_of_manufacture")
    private Integer yearOfManufacture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Employee driver;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "puc_expiry_date")
    private LocalDate pucExpiryDate;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(name = "current_odometer")
    private Integer currentOdometer;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // -- Relationships --

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleFuelEntry> fuelEntries = new ArrayList<>();

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VehicleMaintenance> maintenanceRecords = new ArrayList<>();
}
