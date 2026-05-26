package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "vehicle_maintenance",
    indexes = {
        @Index(name = "idx_vm_vehicle_id",  columnList = "vehicle_id"),
        @Index(name = "idx_vm_service_dt",  columnList = "service_date"),
        @Index(name = "idx_vm_next_svc",    columnList = "next_service_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class VehicleMaintenance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost", nullable = false, precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "service_center", length = 200)
    private String serviceCenter;

    @Column(name = "odometer_at_service")
    private Integer odometerAtService;

    @Column(name = "next_service_date")
    private LocalDate nextServiceDate;

    @Column(name = "next_service_km")
    private Integer nextServiceKm;

    @Column(name = "receipt_path", length = 500)
    private String receiptPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    private User addedBy;
}
