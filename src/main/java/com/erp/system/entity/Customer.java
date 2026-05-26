package com.erp.system.entity;

import com.erp.system.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customers_mobile",  columnList = "mobile"),
        @Index(name = "idx_customers_area",    columnList = "area"),
        @Index(name = "idx_customers_type",    columnList = "customer_type"),
        @Index(name = "idx_customers_city",    columnList = "city"),
        @Index(name = "idx_customers_active",  columnList = "is_active"),
        @Index(name = "idx_customers_created_by", columnList = "created_by_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"orders", "invoices", "payments", "visits", "reminders"})
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "mobile", nullable = false, length = 15)
    private String mobile;

    @Column(name = "alternate_mobile", length = 15)
    private String alternateMobile;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", length = 20)
    private CustomerType customerType;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // -- Relationships --

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerVisit> visits = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CustomerReminder> reminders = new ArrayList<>();
}
