package com.jala.empmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Dashboard statistics response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private long totalEmployees;
    private long activeEmployees;
    private long inactiveEmployees;
    private long adminCount;
    private long userCount;
    private Map<String, Long> employeesByDepartment;
}
