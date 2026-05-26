package com.erp.system.entity;

import com.erp.system.enums.ReminderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "customer_reminders",
    indexes = {
        @Index(name = "idx_cr_customer_id",    columnList = "customer_id"),
        @Index(name = "idx_cr_reminder_date",  columnList = "reminder_date"),
        @Index(name = "idx_cr_is_completed",   columnList = "is_completed")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class CustomerReminder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 20)
    private ReminderType reminderType;

    @Column(name = "reminder_date", nullable = false)
    private LocalDateTime reminderDate;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;
}
