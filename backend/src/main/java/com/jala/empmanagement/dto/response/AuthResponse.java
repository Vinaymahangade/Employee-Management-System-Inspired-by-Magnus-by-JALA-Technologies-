package com.jala.empmanagement.dto.response;

import com.jala.empmanagement.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after a successful login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private EmployeeResponse employee;

    public static AuthResponse of(String token, Employee employee) {
        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .employee(EmployeeResponse.from(employee))
            .build();
    }
}
