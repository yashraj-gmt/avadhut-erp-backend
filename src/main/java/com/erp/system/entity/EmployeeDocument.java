package com.erp.system.entity;

import com.erp.system.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "employee_documents",
    indexes = {
        @Index(name = "idx_ed_employee_id", columnList = "employee_id"),
        @Index(name = "idx_ed_doc_type",    columnList = "document_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id", callSuper = false)
public class EmployeeDocument extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "notes", length = 255)
    private String notes;
}
