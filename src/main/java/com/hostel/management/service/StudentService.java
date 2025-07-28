package com.hostel.management.service;

import com.hostel.management.dto.request.AbsenceRequestDto;
import com.hostel.management.dto.request.StudentRegistrationRequest;
import com.hostel.management.dto.response.StudentDashboardResponse;
import com.hostel.management.entity.Attendance;
import com.hostel.management.entity.Student;

import java.util.List;

/**
 * Service interface for student-related operations
 */
public interface StudentService {
    /**
     * Registers a new student
     *
     * @param request Student registration request
     */
    void registerStudent(StudentRegistrationRequest request);

    /**
     * Submits an absence request for a student
     *
     * @param email Student's email
     * @param request Absence request details
     */
    void submitAbsenceRequest(String email, AbsenceRequestDto request);

    /**
     * Gets attendance history for a student
     *
     * @param email Student's email
     * @param months Number of months to retrieve
     * @return List of attendance records
     */
    List<Attendance> getAttendanceHistory(String email, int months);

    /**
     * Gets student dashboard data
     *
     * @param email Student's email
     * @return Student dashboard response
     */
    StudentDashboardResponse getStudentDashboard(String email);

    /**
     * Finds a student by email
     *
     * @param email Student's email
     * @return Student entity
     */
    Student findByEmail(String email);
}
