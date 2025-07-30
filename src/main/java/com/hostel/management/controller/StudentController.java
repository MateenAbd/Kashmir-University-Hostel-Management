package com.hostel.management.controller;

import com.hostel.management.dto.request.AbsenceRequestDto;
import com.hostel.management.dto.request.StudentRegistrationRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.StudentDashboardResponse;
import com.hostel.management.entity.Attendance;
import com.hostel.management.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> register(@Valid @ModelAttribute StudentRegistrationRequest request) {
        try {
            studentService.registerStudent(request);
            return ResponseEntity.ok(ApiResponse.success("Registration request submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Registration failed: " + e.getMessage(), "REGISTRATION_ERROR"));
        }
    }

    @PostMapping("/absence-request")
    public ResponseEntity<ApiResponse<String>> submitAbsenceRequest(
            @Valid @RequestBody AbsenceRequestDto request,
            Authentication authentication) {

        studentService.submitAbsenceRequest(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Absence request submitted successfully"));
    }

    @GetMapping("/attendance-history")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendanceHistory(
            @RequestParam(defaultValue = "3") int months,
            Authentication authentication) {

        List<Attendance> history = studentService.getAttendanceHistory(authentication.getName(), months);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> getDashboard(Authentication authentication) {
        StudentDashboardResponse dashboard = studentService.getStudentDashboard(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
