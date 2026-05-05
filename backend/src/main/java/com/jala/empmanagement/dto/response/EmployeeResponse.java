package com.jala.empmanagement.dto.response;

import com.jala.empmanagement.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Employee response DTO — never exposes the password field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String name;
    private String email;
    private Employee.Role role;
    private Employee.Status status;
    private String phone;
    private String department;
    private String designation;
    private String profileImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Map Employee entity → EmployeeResponse DTO.
     */
    public static EmployeeResponse from(Employee emp) {
        return EmployeeResponse.builder()
            .id(emp.getId())
            .name(emp.getName())
            .email(emp.getEmail())
            .role(emp.getRole())
            .status(emp.getStatus())
            .phone(emp.getPhone())
            .department(emp.getDepartment())
            .designation(emp.getDesignation())
            .profileImage(emp.getProfileImage())
            .createdAt(emp.getCreatedAt())
            .updatedAt(emp.getUpdatedAt())
            .build();
    }
}
