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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        // Validate input
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new AuthenticationException("Invalid credentials");
        }

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid credentials");
        }

        Boolean isMonitor = false;
        String fullName = null;

        // Only check for monitor status if user is a student
        if (user.getRole() == User.UserRole.STUDENT) {
            Student student = studentRepository.findByUserEmail(user.getEmail()).orElse(null);
            if (student != null) {
                isMonitor = student.getIsMonitor() != null ? student.getIsMonitor() : false;
                fullName = student.getFullName();
            }
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name(), isMonitor);

        return LoginResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .isMonitor(isMonitor)
                .fullName(fullName)
                .build();
    }
}
