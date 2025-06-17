package com.hostel.management.service;

import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public boolean isMonitor(String email) {
        return studentRepository.findByUserEmail(email)
                .map(Student::getIsMonitor)
                .orElse(false);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
