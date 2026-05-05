package com.jala.empmanagement.service;

import com.jala.empmanagement.dto.request.LoginRequest;
import com.jala.empmanagement.dto.response.AuthResponse;

/**
 * Authentication service interface.
 */
public interface AuthService {
    AuthResponse login(LoginRequest request);
}
