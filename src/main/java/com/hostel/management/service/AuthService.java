package com.hostel.management.service;

import com.hostel.management.dto.request.LoginRequest;
import com.hostel.management.dto.response.LoginResponse;

/**
 * Service interface for authentication operations
 */
public interface AuthService {
    /**
     * Authenticates a user and returns login response with JWT token
     *
     * @param request Login credentials
     * @return LoginResponse containing token and user details
     */
    LoginResponse login(LoginRequest request);
}
