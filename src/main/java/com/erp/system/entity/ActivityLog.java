package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "activity_logs",
    indexes = {
        @Index(name = "idx_al_user_id",       columnList = "user_id"),
        @Index(name = "idx_al_module",         columnList = "module"),
        @Index(name = "idx_al_reference",      columnList = "reference_id, module"),
        @Index(name = "idx_al_created_at",     columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Module name: CUSTOMER, ORDER, INVOICE, EMPLOYEE, STOCK, etc. */
    @Column(name = "module", nullable = false, length = 50)
    private String module;

    /** Action performed: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc. */
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;
}
