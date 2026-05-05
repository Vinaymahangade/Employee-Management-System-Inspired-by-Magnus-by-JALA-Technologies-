package com.jala.empmanagement.dto.request;

import com.jala.empmanagement.entity.Employee;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Request DTO for updating an existing employee.
 * All fields are optional — only provided fields will be updated.
 */
@Data
public class UpdateEmployeeRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    private Employee.Role role;

    private Employee.Status status;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String designation;
}
