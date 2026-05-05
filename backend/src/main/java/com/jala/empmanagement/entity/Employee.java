package com.jala.empmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Employee entity representing the employees table in PostgreSQL.
 * Uses soft delete pattern (status = INACTIVE instead of physical delete).
 */
@Entity
@Table(name = "employees",
    indexes = {
        @Index(name = "idx_employee_email", columnList = "email", unique = true),
        @Index(name = "idx_employee_status", columnList = "status"),
        @Index(name = "idx_employee_role", columnList = "role")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Stored as BCrypt-hashed password. Never expose in responses.
     */
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    /**
     * Relative path to uploaded profile image stored on disk.
     */
    @Column(name = "profile_image")
    private String profileImage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Enums ──────────────────────────────────────────────────────────────

    public enum Role {
        ADMIN, USER
    }

    public enum Status {
        ACTIVE, INACTIVE
    }
}
