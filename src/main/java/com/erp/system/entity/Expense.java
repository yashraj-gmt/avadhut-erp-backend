package com.erp.system.entity;

import com.erp.system.enums.ExpenseCategory;
import com.erp.system.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "expenses",
    indexes = {
        @Index(name = "idx_exp_category",   columnList = "category"),
        @Index(name = "idx_exp_date",       columnList = "expense_date"),
        @Index(name = "idx_exp_added_by",   columnList = "added_by_id"),
        @Index(name = "idx_exp_pay_mode",   columnList = "payment_mode")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Expense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private ExpenseCategory category;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private PaymentMode paymentMode;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Path to uploaded receipt/bill (S3 key or local path) */
    @Column(name = "attachment_path", length = 500)
    private String attachmentPath;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private User verifiedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "added_by_id", nullable = false)
    private User addedBy;

    /** Optional: link to a vehicle for fuel/maintenance expenses */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;
}
