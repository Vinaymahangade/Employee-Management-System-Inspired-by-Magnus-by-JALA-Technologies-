package com.jala.empmanagement.service.impl;

import com.jala.empmanagement.dto.request.LoginRequest;
import com.jala.empmanagement.dto.response.AuthResponse;
import com.jala.empmanagement.entity.Employee;
import com.jala.empmanagement.exception.BadRequestException;
import com.jala.empmanagement.repository.EmployeeRepository;
import com.jala.empmanagement.security.JwtUtil;
import com.jala.empmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthService.
 * Handles login and JWT generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // Authenticate using Spring Security
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for: {}", request.getEmail());
            throw new BadRequestException("Invalid email or password");
        }

        // Load user details and generate token
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails);

        Employee employee = employeeRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("Employee not found"));

        // Check if account is active
        if (employee.getStatus() == Employee.Status.INACTIVE) {
            throw new BadRequestException("Account is inactive. Please contact administrator.");
        }

        log.info("Login successful for: {}", request.getEmail());
        return AuthResponse.of(token, employee);
    }
}
