package com.hostel.management.service;

import com.hostel.management.entity.*;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WardenService {
    private final MonthlyExpenseRepository monthlyExpenseRepository;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final DeletionRequestRepository deletionRequestRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public List<DeletionRequest> getPendingDeletionRequests() {
        return deletionRequestRepository.findByStatusOrderByCreatedAtDesc(
                DeletionRequest.RequestStatus.PENDING);
    }

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

    public List<MonthlyExpense> getAllMonthlyExpenses() {
        return monthlyExpenseRepository.findAll();
    }

    public MonthlyExpense getMonthlyExpense(String monthYear) {
        return monthlyExpenseRepository.findByMonthYear(monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("Monthly expense not found for " + monthYear));
    }

    public List<AbsenceRequest> getLateAbsenceRequests() {
        return absenceRequestRepository.findPendingRequestsByTimeCategory(true);
    }

    @Transactional
    public void approveAbsenceRequest(Long requestId, String comments, String wardenEmail) {
        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        User warden = userRepository.findByEmail(wardenEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        request.setStatus(AbsenceRequest.RequestStatus.APPROVED);
        request.setApprovedBy(warden);
        request.setComments(comments);
        request.setApprovedAt(LocalDateTime.now());
        
        absenceRequestRepository.save(request);
    }

    @Transactional
    public void rejectAbsenceRequest(Long requestId, String reason, String wardenEmail) {
        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        User warden = userRepository.findByEmail(wardenEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        request.setStatus(AbsenceRequest.RequestStatus.REJECTED);
        request.setApprovedBy(warden);
        request.setComments(reason);
        request.setApprovedAt(LocalDateTime.now());
        
        absenceRequestRepository.save(request);
    }
}
