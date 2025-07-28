package com.hostel.management.service.impl;

import com.hostel.management.entity.Attendance;
import com.hostel.management.entity.Student;
import com.hostel.management.repository.AttendanceRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    @Override
    @Scheduled(cron = "0 1 0 * * ?") // Run at 12:01 AM every day
    @Transactional
    public void createDailyAttendanceRecords() {
        log.info("Creating daily attendance records for {}", LocalDate.now());
        
        List<Student> allStudents = studentRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Student student : allStudents) {
            // Check if attendance record already exists for today
            if (attendanceRepository.findByStudentAndDate(student, today).isEmpty()) {
                Attendance attendance = Attendance.builder()
                        .student(student)
                        .date(today)
                        .status(Attendance.AttendanceStatus.PRESENT) // Default to present
                        .build();
                
                attendanceRepository.save(attendance);
            }
        }
        
        log.info("Daily attendance records created successfully");
    }
}
