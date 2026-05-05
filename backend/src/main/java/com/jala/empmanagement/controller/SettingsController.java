package com.jala.empmanagement.controller;

import com.jala.empmanagement.dto.request.ChangePasswordRequest;
import com.jala.empmanagement.dto.request.UpdateEmployeeRequest;
import com.jala.empmanagement.dto.response.ApiResponse;
import com.jala.empmanagement.dto.response.EmployeeResponse;
import com.jala.empmanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Settings Controller.
 * Allows the currently logged-in user to update their own profile and password.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Settings", description = "Profile and password management for logged-in user")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final EmployeeService employeeService;

    /**
     * GET /api/settings/profile
     * Returns the currently authenticated user's profile.
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getProfile(Authentication auth) {
        String email = auth.getName();
        EmployeeResponse response = employeeService.searchEmployees(email, null, null,
            org.springframework.data.domain.PageRequest.of(0, 1))
            .getContent().stream().findFirst()
            .orElseThrow(() -> new com.jala.empmanagement.exception.ResourceNotFoundException("Profile not found"));
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", response));
    }

    /**
     * PUT /api/settings/profile
     * Update the currently authenticated user's profile.
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateProfile(
        Authentication auth,
        @Valid @RequestBody UpdateEmployeeRequest request) {

        String email = auth.getName();
        EmployeeResponse response = employeeService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    /**
     * PUT /api/settings/change-password
     * Change the currently authenticated user's password.
     */
    @PutMapping("/change-password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
        Authentication auth,
        @Valid @RequestBody ChangePasswordRequest request) {

        String email = auth.getName();
        employeeService.changePassword(email, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }
}
