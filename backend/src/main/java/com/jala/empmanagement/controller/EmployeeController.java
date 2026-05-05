package com.jala.empmanagement.controller;

import com.jala.empmanagement.dto.request.CreateEmployeeRequest;
import com.jala.empmanagement.dto.request.UpdateEmployeeRequest;
import com.jala.empmanagement.dto.response.ApiResponse;
import com.jala.empmanagement.dto.response.DashboardStats;
import com.jala.empmanagement.dto.response.EmployeeResponse;
import com.jala.empmanagement.entity.Employee;
import com.jala.empmanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Employee REST Controller.
 * Provides CRUD, search, dashboard, autocomplete, and file upload endpoints.
 * All endpoints require JWT authentication (except where noted).
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Employee Management", description = "CRUD operations for employees")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeController {

    private final EmployeeService employeeService;

    // ── Dashboard ──────────────────────────────────────────────────────────

    /**
     * GET /api/employees/dashboard
     * Returns total, active, inactive counts and role/dept breakdowns.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboard() {
        DashboardStats stats = employeeService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Dashboard stats fetched", stats));
    }

    // ── CRUD ───────────────────────────────────────────────────────────────

    /**
     * POST /api/employees
     * Create a new employee. Admin only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create employee (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
        @Valid @RequestBody CreateEmployeeRequest request) {

        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.success("Employee created successfully", response));
    }

    /**
     * GET /api/employees/{id}
     * Get a single employee by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee fetched", response));
    }

    /**
     * PUT /api/employees/{id}
     * Update employee fields. Admin only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee (Admin only)")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
        @PathVariable Long id,
        @Valid @RequestBody UpdateEmployeeRequest request) {

        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", response));
    }

    /**
     * DELETE /api/employees/{id}
     * Soft-delete employee (sets status=INACTIVE). Admin only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete employee - soft delete (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted (deactivated) successfully"));
    }

    // ── Search, Filter & Pagination ────────────────────────────────────────

    /**
     * GET /api/employees/search
     * Combined search + filter + pagination endpoint.
     *
     * Query params:
     *   keyword  - search by name or email (optional)
     *   status   - ACTIVE | INACTIVE (optional)
     *   role     - ADMIN | USER (optional)
     *   page     - 0-based page number (default 0)
     *   size     - page size (default 10)
     *   sortBy   - field to sort by (default: createdAt)
     *   sortDir  - asc | desc (default: desc)
     */
    @GetMapping("/search")
    @Operation(summary = "Search employees with filter and pagination")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> searchEmployees(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Employee.Status status,
        @RequestParam(required = false) Employee.Role role,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmployeeResponse> result = employeeService.searchEmployees(keyword, status, role, pageable);
        return ResponseEntity.ok(ApiResponse.success("Employees fetched", result));
    }

    // ── Autocomplete ───────────────────────────────────────────────────────

    /**
     * GET /api/employees/autocomplete?query=john
     * Returns top 10 name/email matches for autocomplete widget.
     */
    @GetMapping("/autocomplete")
    @Operation(summary = "Autocomplete employee name/email")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> autocomplete(
        @RequestParam String query) {

        List<EmployeeResponse> suggestions = employeeService.getAutocompleteSuggestions(query);
        return ResponseEntity.ok(ApiResponse.success("Suggestions fetched", suggestions));
    }

    // ── File Upload ────────────────────────────────────────────────────────

    /**
     * POST /api/employees/{id}/upload-image
     * Upload profile image for an employee.
     */
    @PostMapping("/{id}/upload-image")
    @Operation(summary = "Upload employee profile image")
    public ResponseEntity<ApiResponse<String>> uploadImage(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file) {

        String filename = employeeService.uploadProfileImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded successfully", "/uploads/" + filename));
    }
}
