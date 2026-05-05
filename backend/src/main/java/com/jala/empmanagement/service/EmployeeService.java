package com.jala.empmanagement.service;

import com.jala.empmanagement.dto.request.ChangePasswordRequest;
import com.jala.empmanagement.dto.request.CreateEmployeeRequest;
import com.jala.empmanagement.dto.request.UpdateEmployeeRequest;
import com.jala.empmanagement.dto.response.DashboardStats;
import com.jala.empmanagement.dto.response.EmployeeResponse;
import com.jala.empmanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Employee service interface defining the business contract.
 * All business logic is implemented in EmployeeServiceImpl.
 */
public interface EmployeeService {

    // CRUD Operations
    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request);

    void deleteEmployee(Long id);  // Soft delete — sets status to INACTIVE

    EmployeeResponse getEmployeeById(Long id);

    // Search, Filter & Pagination
    Page<EmployeeResponse> searchEmployees(
        String keyword,
        Employee.Status status,
        Employee.Role role,
        Pageable pageable
    );

    // Autocomplete
    List<EmployeeResponse> getAutocompleteSuggestions(String query);

    // Dashboard
    DashboardStats getDashboardStats();

    // Profile & Settings
    EmployeeResponse updateProfile(String email, UpdateEmployeeRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    String uploadProfileImage(Long id, MultipartFile file);
}
