package com.erp.system.entity;


import com.erp.system.enums.LeaveStatus;
import com.erp.system.enums.LeaveType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "employee_leaves",
    indexes = {
        @Index(name = "idx_el_employee_id", columnList = "employee_id"),
        @Index(name = "idx_el_status",      columnList = "status"),
        @Index(name = "idx_el_from_date",   columnList = "from_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class EmployeeLeave extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "approved_by_remarks", columnDefinition = "TEXT")
    private String approvedByRemarks;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;
}
