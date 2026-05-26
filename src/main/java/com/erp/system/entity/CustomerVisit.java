package com.erp.system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "customer_visits",
    indexes = {
        @Index(name = "idx_cv_customer_id",  columnList = "customer_id"),
        @Index(name = "idx_cv_visited_by",   columnList = "visited_by_id"),
        @Index(name = "idx_cv_visit_date",   columnList = "visit_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class CustomerVisit extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visited_by_id", nullable = false)
    private User visitedBy;

    @Column(name = "visit_date", nullable = false)
    private LocalDateTime visitDate;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "outcome", length = 255)
    private String outcome;
}
