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
        try {
            LocalDate today = LocalDate.now();
            log.info("Creating daily attendance records for {}", today);

            List<Student> allStudents = studentRepository.findAll();

            if (allStudents.isEmpty()) {
                log.info("No students found, skipping attendance record creation");
                return;
            }

            int recordsCreated = 0;
            for (Student student : allStudents) {
                try {
                    // Check if attendance record already exists for today
                    if (attendanceRepository.findByStudentAndDate(student, today).isEmpty()) {
                        Attendance attendance = Attendance.builder()
                                .student(student)
                                .date(today)
                                .status(Attendance.AttendanceStatus.PRESENT) // Default to present
                                .build();

                        attendanceRepository.save(attendance);
                        recordsCreated++;
                    }
                } catch (Exception e) {
                    log.error("Failed to create attendance record for student {}: {}",
                            student.getStudentId(), e.getMessage());
                    // Continue with other students
                }
            }

            log.info("Daily attendance records created successfully. Records created: {}", recordsCreated);
        } catch (Exception e) {
            log.error("Failed to create daily attendance records: {}", e.getMessage(), e);
            // Don't rethrow - this is a scheduled task and should not fail the application
        }
    }
}
