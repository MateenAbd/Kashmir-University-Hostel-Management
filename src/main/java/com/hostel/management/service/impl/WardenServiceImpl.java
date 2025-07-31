package com.hostel.management.service.impl;

import com.hostel.management.entity.*;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import com.hostel.management.service.WardenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WardenServiceImpl implements WardenService {
    private final MonthlyExpenseRepository monthlyExpenseRepository;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final DeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;

    @Override
    public List<DeletionRequest> getPendingDeletionRequests() {
        return deletionRequestRepository.findByStatusOrderByCreatedAtDesc(
                DeletionRequest.RequestStatus.PENDING);

    }
//@Override
//public List<DeletionRequest> getPendingDeletionRequests() {
//    List<DeletionRequest> requests = deletionRequestRepository.findByStatusOrderByCreatedAtDesc(DeletionRequest.RequestStatus.PENDING);
//    System.out.println("WardenService: Found " + requests.size() + " pending deletion requests");
//    return requests;
//}


    @Override
    @Transactional
    public void approveDeletionRequest(Long requestId, String wardenEmail) {
        DeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deletion request not found"));

        User warden = userRepository.findByEmail(wardenEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (request.getStatus() != DeletionRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        // Update request status
        request.setStatus(DeletionRequest.RequestStatus.APPROVED);
        request.setReviewedBy(warden);
        request.setReviewedAt(LocalDateTime.now());
        deletionRequestRepository.save(request);

        // Permanently delete student and related data
        Student student = request.getStudent();

        // Delete student record (CASCADE will handle related records)
        userRepository.deleteById(student.getUser().getUserId());
    }

    @Override
    @Transactional
    public void rejectDeletionRequest(Long requestId, String reason, String wardenEmail) {
        DeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Deletion request not found"));

        User warden = userRepository.findByEmail(wardenEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (request.getStatus() != DeletionRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        request.setStatus(DeletionRequest.RequestStatus.REJECTED);
        request.setReviewedBy(warden);
        request.setReviewedAt(LocalDateTime.now());
        deletionRequestRepository.save(request);
    }

    @Override
    public List<MonthlyExpense> getAllMonthlyExpenses() {
        return monthlyExpenseRepository.findAll();
    }

    @Override
    public MonthlyExpense getMonthlyExpense(String monthYear) {
        return monthlyExpenseRepository.findByMonthYear(monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("Monthly expense not found for " + monthYear));
    }

    @Override
    public List<AbsenceRequest> getLateAbsenceRequests() {
        return absenceRequestRepository.findPendingRequestsByTimeCategory(true);
    }

    @Override
    @Transactional
    public void approveAbsenceRequest(Long requestId, String comments, String wardenEmail) {
        if (requestId == null) {
            throw new BusinessException("Request ID is required");
        }

        if (wardenEmail == null || wardenEmail.trim().isEmpty()) {
            throw new BusinessException("Warden email is required");
        }

        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        User warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (warden.getRole() != User.UserRole.WARDEN) {
            throw new BusinessException("Only wardens can approve absence requests");
        }

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        if (!Boolean.TRUE.equals(request.getIsLateRequest())) {
            throw new BusinessException("Warden can only approve late absence requests");
        }

        request.setStatus(AbsenceRequest.RequestStatus.APPROVED);
        request.setApprovedBy(warden);
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
    public void rejectAbsenceRequest(Long requestId, String reason, String wardenEmail) {
        // Validate input
        if (requestId == null) {
            throw new BusinessException("Request ID is required");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("Rejection reason is required");
        }

        if (wardenEmail == null || wardenEmail.trim().isEmpty()) {
            throw new BusinessException("Warden email is required");
        }

        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        User warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (warden.getRole() != User.UserRole.WARDEN) {
            throw new BusinessException("Only wardens can reject absence requests");
        }

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        // Wardens should only handle late requests
        if (!Boolean.TRUE.equals(request.getIsLateRequest())) {
            throw new BusinessException("Warden can only reject late absence requests");
        }

        request.setStatus(AbsenceRequest.RequestStatus.REJECTED);
        request.setApprovedBy(warden);
        request.setComments(reason.trim());
        request.setApprovedAt(LocalDateTime.now());

        absenceRequestRepository.save(request);
    }
}
