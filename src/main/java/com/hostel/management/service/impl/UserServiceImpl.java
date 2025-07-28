package com.hostel.management.service.impl;

import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Override
    public boolean isMonitor(String email) {
        return studentRepository.findByUserEmail(email)
                .map(Student::getIsMonitor)
                .orElse(false);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
