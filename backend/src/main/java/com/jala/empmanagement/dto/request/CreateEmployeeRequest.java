package com.jala.empmanagement.dto.request;

import com.jala.empmanagement.entity.Employee;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for creating a new employee.
 * Uses Bean Validation annotations for input validation.
 */
@Data
public class CreateEmployeeRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private Employee.Role role;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Size(max = 100, message = "Department max 100 chars")
    private String department;

    @Size(max = 100, message = "Designation max 100 chars")
    private String designation;
}
