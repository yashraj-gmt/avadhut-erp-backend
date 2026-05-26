package com.erp.system.entity;

import com.erp.system.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_role",  columnList = "role"),
        @Index(name = "idx_users_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = {"employee", "customers", "tasks"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "mobile", length = 15)
    private String mobile;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
    // -- Relationships --

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private Employee employee;

    @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY)
    private List<Customer> customers = new ArrayList<>();

    @OneToMany(mappedBy = "assignedBy", fetch = FetchType.LAZY)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();
}
