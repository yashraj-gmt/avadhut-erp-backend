package com.erp.system.entity;

import com.erp.system.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notif_user_id",  columnList = "user_id"),
        @Index(name = "idx_notif_is_read",  columnList = "is_read"),
        @Index(name = "idx_notif_created",  columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 20)
    private NotificationChannel channel = NotificationChannel.SYSTEM;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /** Generic link to the triggering entity */
    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 30)
    private String referenceType;
}
