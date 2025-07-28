package com.hostel.management.service.impl;

import com.hostel.management.entity.AbsenceRequest;
import com.hostel.management.entity.Student;
import com.hostel.management.entity.User;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.AbsenceRequestRepository;
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

    @Override
    public List<AbsenceRequest> getEarlyAbsenceRequests() {
        return absenceRequestRepository.findPendingRequestsByTimeCategory(false);
    }

    @Override
    @Transactional
    public void approveAbsenceRequest(Long requestId, String comments, String monitorEmail) {
        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        Student monitor = studentRepository.findByUserEmail(monitorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));

        if (!monitor.getIsMonitor()) {
            throw new BusinessException("User is not authorized as monitor");
        }

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        if (request.getIsLateRequest()) {
            throw new BusinessException("Monitor can only approve requests submitted before cutoff time");
        }

        request.setStatus(AbsenceRequest.RequestStatus.APPROVED);
        request.setApprovedBy(monitor.getUser());
        request.setComments(comments);
        request.setApprovedAt(LocalDateTime.now());
        
        absenceRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectAbsenceRequest(Long requestId, String reason, String monitorEmail) {
        AbsenceRequest request = absenceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Absence request not found"));

        Student monitor = studentRepository.findByUserEmail(monitorEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Monitor not found"));

        if (!monitor.getIsMonitor()) {
            throw new BusinessException("User is not authorized as monitor");
        }

        if (request.getStatus() != AbsenceRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        if (request.getIsLateRequest()) {
            throw new BusinessException("Monitor can only handle requests submitted before cutoff time");
        }

        request.setStatus(AbsenceRequest.RequestStatus.REJECTED);
        request.setApprovedBy(monitor.getUser());
        request.setComments(reason);
        request.setApprovedAt(LocalDateTime.now());
        
        absenceRequestRepository.save(request);
    }
}
