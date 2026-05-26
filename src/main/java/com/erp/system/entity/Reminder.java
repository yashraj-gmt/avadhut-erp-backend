package com.erp.system.entity;

import com.erp.system.enums.NotificationChannel;
import com.erp.system.enums.ReferenceType;
import com.erp.system.enums.ReminderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reminders",
    indexes = {
        @Index(name = "idx_rem_type",         columnList = "reminder_type"),
        @Index(name = "idx_rem_date",         columnList = "reminder_date"),
        @Index(name = "idx_rem_sent",         columnList = "is_sent"),
        @Index(name = "idx_rem_ref",          columnList = "reference_id, reference_type"),
        @Index(name = "idx_rem_created_by",   columnList = "created_by_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Reminder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "reminder_type", nullable = false, length = 20)
    private ReminderType reminderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "reminder_date", nullable = false)
    private LocalDateTime reminderDate;

    @Column(name = "is_sent", nullable = false)
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** Generic foreign key to any entity */
    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 20)
    private ReferenceType referenceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    /** Recipient user (if system notification) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id")
    private User recipientUser;
}
