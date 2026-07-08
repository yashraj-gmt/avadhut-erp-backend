package com.erp.system.entity;

import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customers_mobile",     columnList = "mobile"),
        @Index(name = "idx_customers_area",       columnList = "area"),
        @Index(name = "idx_customers_type",       columnList = "customer_type"),
        @Index(name = "idx_customers_city",       columnList = "city"),
        @Index(name = "idx_customers_active",     columnList = "is_active"),
        @Index(name = "idx_customers_regular",    columnList = "is_regular"),
        @Index(name = "idx_customers_status",     columnList = "customer_status"),
        @Index(name = "idx_customers_date_joined",columnList = "date_joined"),
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

    /** Granular status: ACTIVE / INACTIVE / BLOCKED */
    @Enumerated(EnumType.STRING)
    @Column(name = "customer_status", nullable = false, length = 20)
    private CustomerStatus customerStatus = CustomerStatus.ACTIVE;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Set by the regular-customer algorithm/scheduler.
     * True when the customer meets the configurable threshold
     * (e.g. ≥N orders in last X months, or total spend ≥ threshold).
     */
    @Column(name = "is_regular", nullable = false)
    private Boolean isRegular = false;

    /** Date when the customer was first flagged as regular. Cleared when flag is removed. */
    @Column(name = "regular_since")
    private LocalDate regularSince;

    /**
     * Explicit date-joined field — defaults to today on first persist.
     * Separate from createdAt so historical/imported customers can have
     * a different join date from their DB creation timestamp.
     */
    @Column(name = "date_joined", nullable = false)
    private LocalDate dateJoined;

    @PrePersist
    private void setDefaults() {
        if (dateJoined == null) {
            dateJoined = LocalDate.now();
        }
    }

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

