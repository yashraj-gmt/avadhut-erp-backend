package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "employees",
    indexes = {
        @Index(name = "idx_emp_mobile",     columnList = "mobile"),
        @Index(name = "idx_emp_department", columnList = "department"),
        @Index(name = "idx_emp_active",     columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"user", "attendances", "tasks", "salaries", "documents"})
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optional link to a User account.
     * Employees who need system login will have a User record.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

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

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "basic_salary", precision = 15, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "bank_account_number", length = 30)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "ifsc_code", length = 15)
    private String ifscCode;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // -- Relationships --

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmployeeDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Attendance> attendances = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmployeeLeave> leaves = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Salary> salaries = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo", fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();
}
