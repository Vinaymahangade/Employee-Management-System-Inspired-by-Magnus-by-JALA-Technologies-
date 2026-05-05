package com.jala.empmanagement.service.impl;

import com.jala.empmanagement.dto.request.ChangePasswordRequest;
import com.jala.empmanagement.dto.request.CreateEmployeeRequest;
import com.jala.empmanagement.dto.request.UpdateEmployeeRequest;
import com.jala.empmanagement.dto.response.DashboardStats;
import com.jala.empmanagement.dto.response.EmployeeResponse;
import com.jala.empmanagement.entity.Employee;
import com.jala.empmanagement.exception.BadRequestException;
import com.jala.empmanagement.exception.ResourceNotFoundException;
import com.jala.empmanagement.repository.EmployeeRepository;
import com.jala.empmanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeeService.
 * Contains all business logic for employee management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // ── CRUD Operations ────────────────────────────────────────────────────

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        log.info("Creating new employee with email: {}", request.getEmail());

        // Check for duplicate email
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        Employee employee = Employee.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(request.getRole())
            .phone(request.getPhone())
            .department(request.getDepartment())
            .designation(request.getDesignation())
            .status(Employee.Status.ACTIVE)
            .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Employee created successfully with ID: {}", saved.getId());
        return EmployeeResponse.from(saved);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("Updating employee ID: {}", id);

        Employee employee = findEmployeeById(id);

        // Check email uniqueness if changed
        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(employee.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use: " + request.getEmail());
        }

        // Apply non-null fields only (partial update)
        if (StringUtils.hasText(request.getName())) employee.setName(request.getName());
        if (StringUtils.hasText(request.getEmail())) employee.setEmail(request.getEmail());
        if (request.getRole() != null)   employee.setRole(request.getRole());
        if (request.getStatus() != null) employee.setStatus(request.getStatus());
        if (StringUtils.hasText(request.getPhone()))       employee.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getDepartment()))  employee.setDepartment(request.getDepartment());
        if (StringUtils.hasText(request.getDesignation())) employee.setDesignation(request.getDesignation());

        Employee updated = employeeRepository.save(employee);
        log.info("Employee updated successfully: {}", id);
        return EmployeeResponse.from(updated);
    }

    @Override
    public void deleteEmployee(Long id) {
        log.info("Soft deleting employee ID: {}", id);
        Employee employee = findEmployeeById(id);
        employee.setStatus(Employee.Status.INACTIVE);
        employeeRepository.save(employee);
        log.info("Employee soft-deleted: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(Long id) {
        return EmployeeResponse.from(findEmployeeById(id));
    }

    // ── Search & Pagination ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> searchEmployees(
        String keyword,
        Employee.Status status,
        Employee.Role role,
        Pageable pageable
    ) {
        log.debug("Searching employees: keyword={}, status={}, role={}", keyword, status, role);
        return employeeRepository.searchEmployees(keyword, status, role, pageable)
            .map(EmployeeResponse::from);
    }

    // ── Autocomplete ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAutocompleteSuggestions(String query) {
        if (!StringUtils.hasText(query) || query.length() < 2) return List.of();
        Pageable limit = PageRequest.of(0, 10);
        return employeeRepository.findSuggestions(query, limit)
            .stream()
            .map(EmployeeResponse::from)
            .collect(Collectors.toList());
    }

    // ── Dashboard Stats ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long total    = employeeRepository.count();
        long active   = employeeRepository.countByStatus(Employee.Status.ACTIVE);
        long inactive = employeeRepository.countByStatus(Employee.Status.INACTIVE);
        long admins   = employeeRepository.countByRole(Employee.Role.ADMIN);
        long users    = employeeRepository.countByRole(Employee.Role.USER);

        // Build department breakdown map
        Map<String, Long> byDept = employeeRepository.countByDepartment()
            .stream()
            .collect(Collectors.toMap(
                row -> row[0] != null ? (String) row[0] : "Unknown",
                row -> (Long) row[1]
            ));

        return DashboardStats.builder()
            .totalEmployees(total)
            .activeEmployees(active)
            .inactiveEmployees(inactive)
            .adminCount(admins)
            .userCount(users)
            .employeesByDepartment(byDept)
            .build();
    }

    // ── Profile & Settings ─────────────────────────────────────────────────

    @Override
    public EmployeeResponse updateProfile(String email, UpdateEmployeeRequest request) {
        log.info("Updating profile for: {}", email);
        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));

        if (StringUtils.hasText(request.getName())) employee.setName(request.getName());
        if (StringUtils.hasText(request.getPhone())) employee.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getDepartment())) employee.setDepartment(request.getDepartment());
        if (StringUtils.hasText(request.getDesignation())) employee.setDesignation(request.getDesignation());

        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Changing password for: {}", email);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        Employee employee = employeeRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employeeRepository.save(employee);
        log.info("Password changed successfully for: {}", email);
    }

    @Override
    public String uploadProfileImage(Long id, MultipartFile file) {
        log.info("Uploading profile image for employee ID: {}", id);
        Employee employee = findEmployeeById(id);

        try {
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed");
            }

            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String filename = "emp_" + id + "_" + System.currentTimeMillis() + "." + extension;
            Path filePath = uploadPath.resolve(filename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Delete old image if exists
            if (StringUtils.hasText(employee.getProfileImage())) {
                try {
                    Files.deleteIfExists(Paths.get(uploadDir + employee.getProfileImage()));
                } catch (IOException ignored) { /* ignore if old file not found */ }
            }

            employee.setProfileImage(filename);
            employeeRepository.save(employee);
            log.info("Profile image uploaded: {}", filename);
            return filename;

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new BadRequestException("Failed to upload image: " + e.getMessage());
        }
    }

    // ── Private Helpers ────────────────────────────────────────────────────

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
}
