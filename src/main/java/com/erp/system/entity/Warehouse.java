package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "warehouses",
        indexes = {
                @Index(name = "idx_warehouse_name",   columnList = "name"),
                @Index(name = "idx_warehouse_code",   columnList = "code"),
                @Index(name = "idx_warehouse_active", columnList = "is_active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = "inventories")
@SQLRestriction("deleted = false")
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "code", unique = true, nullable = false, length = 30)
    private String code;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /** Total physical storage capacity of this warehouse */
    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Inventory> inventories = new ArrayList<>();
}