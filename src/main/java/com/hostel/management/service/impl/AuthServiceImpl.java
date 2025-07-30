package com.hostel.management.service.impl;

import com.hostel.management.dto.request.LoginRequest;
import com.hostel.management.dto.response.LoginResponse;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.exception.AuthenticationException;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.security.JwtTokenProvider;
import com.hostel.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.debug("=== LOGIN ATTEMPT START ===");

        // Validate input
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            log.debug("Invalid input detected - request: {}, email: {}, password: {}",
                    request != null,
                    request != null ? request.getEmail() != null : "null request",
                    request != null ? (request.getPassword() != null ? "provided" : "null") : "null request");
            throw new AuthenticationException("Invalid credentials");
        }

        log.debug("Original email: '{}'", request.getEmail());
        log.debug("Password length: {}", request.getPassword().length());

        String processedEmail = request.getEmail().trim().toLowerCase();
        log.debug("Processed email (trimmed & lowercase): '{}'", processedEmail);

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(processedEmail);
        log.debug("Database query result - User found: {}", userOptional.isPresent());

        if (userOptional.isEmpty()) {
            log.debug("User not found for email: '{}' - throwing AuthenticationException", processedEmail);
            throw new AuthenticationException("Invalid credentials");
        }

        User user = userOptional.get();
        log.debug("User found - ID: {}, Email: '{}', Role: {}",
                user.getUserId(), user.getEmail(), user.getRole());
        log.debug("User password hash - Length: {}, Starts with: {}",
                user.getPasswordHash() != null ? user.getPasswordHash().length() : "null",
                user.getPasswordHash() != null ? user.getPasswordHash().substring(0, Math.min(10, user.getPasswordHash().length())) + "..." : "null");

        // Check password
        log.debug("Checking password match...");
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        log.debug("Password matches: {}", passwordMatches);

        if (!passwordMatches) {
            log.debug("Password mismatch for user: '{}' - throwing AuthenticationException", processedEmail);
            throw new AuthenticationException("Invalid credentials");
        }

        log.debug("Password verification successful");

        // Initialize default values
        Boolean isMonitor = false;
        String fullName = null;

        // Only check for monitor status if user is a student
        log.debug("Checking user role: {}", user.getRole());
        if (user.getRole() == User.UserRole.STUDENT) {
            log.debug("User is STUDENT - checking for student record and monitor status");
            Optional<Student> studentOptional = studentRepository.findByUserEmail(user.getEmail());
            log.debug("Student record found: {}", studentOptional.isPresent());

            if (studentOptional.isPresent()) {
                Student student = studentOptional.get();
                isMonitor = student.getIsMonitor() != null ? student.getIsMonitor() : false;
                fullName = student.getFullName();
                log.debug("Student details - Full name: '{}', Is monitor: {}", fullName, isMonitor);
            } else {
                log.debug("No student record found for user email: '{}'", user.getEmail());
            }
        } else {
            log.debug("User is not a STUDENT (role: {}), skipping student-specific checks", user.getRole());
        }

        log.debug("Generating JWT token for user: '{}', role: '{}', isMonitor: {}",
                user.getEmail(), user.getRole().name(), isMonitor);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name(), isMonitor);
        log.debug("JWT token generated successfully - Length: {}", token.length());

        LoginResponse response = LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .isMonitor(isMonitor)
                .fullName(fullName)
                .build();

        log.debug("Login response created - Email: '{}', Role: {}, IsMonitor: {}, FullName: '{}'",
                response.getEmail(), response.getRole(), response.getIsMonitor(), response.getFullName());
        log.debug("=== LOGIN ATTEMPT SUCCESSFUL ===");

        return response;
    }
}
