package com.hostel.management.service.impl;

import com.hostel.management.entity.AbsenceRequest;
import com.hostel.management.entity.Attendance;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.AbsenceRequestRepository;
import com.hostel.management.repository.AttendanceRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {
    private final AbsenceRequestRepository absenceRequestRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public List<AbsenceRequest> getEarlyAbsenceRequests() {
        return absenceRequestRepository.findPendingRequestsByTimeCategory(false);
    }
//@Override
//public List<AbsenceRequest> getEarlyAbsenceRequests() {
//    try {
//        System.out.println("=== SERVICE DEBUG ===");
//        System.out.println("Getting early absence requests from service...");
//
//        List<AbsenceRequest> requests = absenceRequestRepository.findPendingRequestsByTimeCategory(false);
//        System.out.println("Repository returned " + requests.size() + " requests");
//
//        // Debug each request
//        for (AbsenceRequest request : requests) {
//            System.out.println("Processing request ID: " + request.getRequestId());
//            System.out.println("Student: " + request.getStudent().getFullName());
//            System.out.println("Status: " + request.getStatus());
//            System.out.println("Is late request: " + request.getIsLateRequest());
//        }
//
//        return requests;
//    } catch (Exception e) {
//        System.out.println("=== SERVICE ERROR ===");
//        System.out.println("Error in getEarlyAbsenceRequests service: " + e.getMessage());
//        e.printStackTrace();
//        throw new RuntimeException("Service error: " + e.getMessage(), e);
//    }
//}



    @Override
    @Transactional
    public void approveAbsenceRequest(Long requestId, String comments, String monitorEmail) {
        // Validate input
        if (requestId == null) {
            throw new BusinessException("Request ID is required");
        }

        if (monitorEmail == null || monitorEmail.trim().isEmpty()) {
            throw new BusinessException("Monitor email is required");
        }

        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        Student monitor = studentRepository.findByUserEmail(monitorEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));

        if (!Boolean.TRUE.equals(monitor.getIsMonitor())) {
            throw new BusinessException("User is not authorized as monitor");
        }

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        if (Boolean.TRUE.equals(request.getIsLateRequest())) {
            throw new BusinessException("Monitor can only approve requests submitted before cutoff time");
        }

        // Don't allow monitors to approve their own requests
//        if (request.getStudent().getStudentId().equals(monitor.getStudentId())) {
//            throw new BusinessException("Monitor cannot approve their own absence request");
//        }

        request.setStatus(AbsenceRequest.RequestStatus.APPROVED);
        request.setApprovedBy(monitor.getUser());
        request.setComments(comments != null ? comments.trim() : null);
        request.setApprovedAt(LocalDateTime.now());

        absenceRequestRepository.save(request);

        // Update or create attendance
        Attendance attendance = attendanceRepository.findByStudentAndDate(
                request.getStudent(), request.getAbsenceDate()).orElse(null);

        if (attendance != null) {
            attendance.setStatus(Attendance.AttendanceStatus.ABSENT);
            attendance.setApprovedAt(LocalDateTime.now());
            attendanceRepository.save(attendance);
        } else {
            Attendance newAttendance = Attendance.builder()
                    .student(request.getStudent())
                    .date(request.getAbsenceDate())
                    .status(Attendance.AttendanceStatus.ABSENT)
                    .createdAt(LocalDateTime.now())
                    .approvedAt(LocalDateTime.now())
                    .build();
            attendanceRepository.save(newAttendance);
        }
    }

    @Override
    @Transactional
    public void rejectAbsenceRequest(Long requestId, String reason, String monitorEmail) {
        // Validate input
        if (requestId == null) {
            throw new BusinessException("Request ID is required");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("Rejection reason is required");
        }

        if (monitorEmail == null || monitorEmail.trim().isEmpty()) {
            throw new BusinessException("Monitor email is required");
        }

        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        Student monitor = studentRepository.findByUserEmail(monitorEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));

        if (!Boolean.TRUE.equals(monitor.getIsMonitor())) {
            throw new BusinessException("User is not authorized as monitor");
        }

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        if (Boolean.TRUE.equals(request.getIsLateRequest())) {
            throw new BusinessException("Monitor can only handle requests submitted before cutoff time");
        }

        // Don't allow monitors to reject their own requests
        if (request.getStudent().getStudentId().equals(monitor.getStudentId())) {
            throw new BusinessException("Monitor cannot reject their own absence request");
        }

        request.setStatus(AbsenceRequest.RequestStatus.REJECTED);
        request.setApprovedBy(monitor.getUser());
        request.setComments(reason.trim());
        request.setApprovedAt(LocalDateTime.now());

        absenceRequestRepository.save(request);
    }
}
