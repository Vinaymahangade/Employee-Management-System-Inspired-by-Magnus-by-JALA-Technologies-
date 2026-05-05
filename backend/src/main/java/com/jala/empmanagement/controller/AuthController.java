package com.jala.empmanagement.controller;

import com.jala.empmanagement.dto.request.LoginRequest;
import com.jala.empmanagement.dto.response.ApiResponse;
import com.jala.empmanagement.dto.response.AuthResponse;
import com.jala.empmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller.
 * Public endpoint — no JWT required.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Authenticates user credentials and returns a JWT token.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email and password to get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
        @Valid @RequestBody LoginRequest request) {

        log.info("Login request for: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
