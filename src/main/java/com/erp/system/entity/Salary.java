package com.erp.system.entity;

import com.erp.system.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "salaries",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_salary_emp_month_year",
            columnNames = {"employee_id", "salary_month", "salary_year"}
        )
    },
    indexes = {
        @Index(name = "idx_sal_employee_id",   columnList = "employee_id"),
        @Index(name = "idx_sal_month_year",    columnList = "salary_month, salary_year"),
        @Index(name = "idx_sal_payment_date",  columnList = "payment_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Salary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "salary_month", nullable = false)
    private Integer salaryMonth;   // 1-12

    @Column(name = "salary_year", nullable = false)
    private Integer salaryYear;

    @Column(name = "basic_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal basicSalary;

    @Column(name = "advance_deduction", precision = 15, scale = 2)
    private BigDecimal advanceDeduction = BigDecimal.ZERO;

    @Column(name = "other_deductions", precision = 15, scale = 2)
    private BigDecimal otherDeductions = BigDecimal.ZERO;

    @Column(name = "bonus", precision = 15, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;

    @Column(name = "allowances", precision = 15, scale = 2)
    private BigDecimal allowances = BigDecimal.ZERO;

    @Column(name = "net_salary", nullable = false, precision = 15, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "present_days")
    private Integer presentDays;

    @Column(name = "absent_days")
    private Integer absentDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 20)
    private PaymentMode paymentMode;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_id")
    private User processedBy;
}
