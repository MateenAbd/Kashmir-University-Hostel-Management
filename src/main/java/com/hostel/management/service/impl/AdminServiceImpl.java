package com.hostel.management.service.impl;

import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.response.RegistrationRequestResponse;
import com.hostel.management.entity.*;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import com.hostel.management.service.AdminService;
import com.hostel.management.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final FormNumberRepository formNumberRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final MonthlyExpenseRepository monthlyExpenseRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final AttendanceRepository attendanceRepository;
    private final DeletionRequestRepository deletionRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final BillingService billingService;

    @Override
    @Transactional
    public void addFormNumbers(List<String> formNumbers, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        List<FormNumber> formNumberEntities = formNumbers.stream()
                .filter(formNumber -> !formNumberRepository.existsByFormNumber(formNumber))
                .map(formNumber -> FormNumber.builder()
                        .formNumber(formNumber)
                        .admin(admin)
                        .build())
                .toList();

        formNumberRepository.saveAll(formNumberEntities);
    }

    @Override
    public List<RegistrationRequest> getPendingRegistrationRequests() {
        return registrationRequestRepository.findByStatusOrderByCreatedAtDesc(
                RegistrationRequest.RequestStatus.PENDING);
    }

    @Override
    public List<RegistrationRequestResponse> getPendingRegistrationRequestResponses() {
        return getPendingRegistrationRequests()
                .stream()
                .map(req -> RegistrationRequestResponse.builder()
                        .requestId(req.getRequestId())
                        .formNumber(req.getFormNumber() != null ? req.getFormNumber().getFormNumber() : null)
                        .email(req.getEmail())
                        .enrollmentNo(req.getEnrollmentNo())
                        .fullName(req.getFullName())
                        .phone(req.getPhone())
                        .department(req.getDepartment())
                        .batch(req.getBatch())
                        .pincode(req.getPincode())
                        .district(req.getDistrict())
                        .tehsil(req.getTehsil())
                        .guardianPhone(req.getGuardianPhone())
                        .photoUrl(req.getPhotoUrl())
                        .status(req.getStatus().name())
                        .comments(req.getComments())
                        .reviewedBy(req.getReviewedBy() != null ? req.getReviewedBy().getEmail() : null)
                        .reviewedAt(req.getReviewedAt())
                        .createdAt(req.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void approveRegistrationRequest(Long requestId, String adminEmail) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (request.getStatus() != RegistrationRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        // Create user account
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode("defaultPassword123")) // Should be changed on first login
                .role(User.UserRole.STUDENT)
                .build();

        user = userRepository.save(user);

        // Create student record
        Student student = Student.builder()
                .user(user)
                .enrollmentNo(request.getEnrollmentNo())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .batch(request.getBatch())
                .pincode(request.getPincode())
                .district(request.getDistrict())
                .tehsil(request.getTehsil())
                .guardianPhone(request.getGuardianPhone())
                .photoUrl(request.getPhotoUrl())
                .build();

        studentRepository.save(student);

        // Update request status
        request.setStatus(RegistrationRequest.RequestStatus.APPROVED);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        registrationRequestRepository.save(request);
    }

    @Override
    @Transactional
    public void rejectRegistrationRequest(Long requestId, String comments, String adminEmail) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (request.getStatus() != RegistrationRequest.RequestStatus.PENDING) {
            throw new BusinessException("Request has already been processed");
        }

        request.setStatus(RegistrationRequest.RequestStatus.REJECTED);
        request.setComments(comments);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        registrationRequestRepository.save(request);

        // Free up the form number
        FormNumber formNumber = request.getFormNumber();
        formNumber.setIsUsed(false);
        formNumberRepository.save(formNumber);
    }

    @Override
    @Transactional
    public void assignMonitorRole(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Revoke existing monitor role
        studentRepository.clearAllMonitors();

        // Assign new monitor role
        student.setIsMonitor(true);
        studentRepository.save(student);
    }

    @Override
    @Transactional
    public void enterMonthlyExpense(YearMonth monthYear, BigDecimal totalAmount, String adminEmail) {
        String monthYearStr = monthYear.toString();
        
        if (monthlyExpenseRepository.existsByMonthYear(monthYearStr)) {
            throw new BusinessException("Monthly expense already exists for " + monthYear);
        }

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        MonthlyExpense expense = MonthlyExpense.builder()
                .monthYear(monthYearStr)
                .totalAmount(totalAmount)
                .enteredBy(admin)
                .build();

        monthlyExpenseRepository.save(expense);

        // Generate bills for all students
        generateMonthlyBills(monthYear, totalAmount);
    }

    private void generateMonthlyBills(YearMonth monthYear, BigDecimal totalAmount) {
        List<Student> students = studentRepository.findAll();
        Integer totalPresentDays = attendanceRepository.countTotalPresentDaysByMonth(
                monthYear.getYear(), monthYear.getMonthValue());

        if (totalPresentDays == null || totalPresentDays == 0) {
            return; // No attendance data available
        }

        BigDecimal perDayRate = totalAmount.divide(BigDecimal.valueOf(totalPresentDays), 2, RoundingMode.HALF_UP);

        for (Student student : students) {
            Integer studentPresentDays = attendanceRepository.countPresentDaysByStudentAndMonth(
                    student, monthYear.getYear(), monthYear.getMonthValue());

            if (studentPresentDays != null && studentPresentDays > 0) {
                BigDecimal amountDue = perDayRate.multiply(BigDecimal.valueOf(studentPresentDays));

                Bill bill = Bill.builder()
                        .student(student)
                        .monthYear(monthYear.toString())
                        .amountDue(amountDue)
                        .presentDays(studentPresentDays)
                        .totalDays(monthYear.lengthOfMonth())
                        .build();

                billRepository.save(bill);
            }
        }
    }

    @Override
    @Transactional
    public void recordPayment(PaymentRequest paymentRequest, String adminEmail) {
        Student student = studentRepository.findById(paymentRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Simply add funds to student balance
        billingService.addFundsToStudentBalance(student, paymentRequest.getAmount());
    }

    @Override
    @Transactional
    public void requestStudentDeletion(Long studentId, String reason, String adminEmail) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        DeletionRequest deletionRequest = DeletionRequest.builder()
                .student(student)
                .requestedBy(admin)
                .reason(reason)
                .build();

        deletionRequestRepository.save(deletionRequest);
    }
}
