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
        // Validate input
        if (monthYear == null) {
            throw new BusinessException("Month year is required");
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Total amount must be greater than zero");
        }

        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            throw new BusinessException("Admin email is required");
        }

        // Don't allow future months beyond next month
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        if (monthYear.isAfter(nextMonth)) {
            throw new BusinessException("Cannot enter expenses for months beyond next month");
        }

        String monthYearStr = monthYear.toString();

        if (monthlyExpenseRepository.existsByMonthYear(monthYearStr)) {
            throw new BusinessException("Monthly expense already exists for " + monthYear);
        }

        User admin = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException("Only admins can enter monthly expenses");
        }

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

        if (students.isEmpty()) {
            return; // No students to generate bills for
        }

        // Get total present days of ALL students combined for the month
        Integer totalPresentDaysAllStudents = attendanceRepository.countTotalPresentDaysByMonth(
                monthYear.getYear(), monthYear.getMonthValue());

        if (totalPresentDaysAllStudents == null || totalPresentDaysAllStudents == 0) {
            // If no attendance data, create bills with zero amount
            for (Student student : students) {
                Bill bill = Bill.builder()
                        .student(student)
                        .monthYear(monthYear.toString())
                        .amountDue(BigDecimal.ZERO)
                        .presentDays(0)
                        .totalDays(monthYear.lengthOfMonth())
                        .build();
                billRepository.save(bill);
            }
            return;
        }

        // Calculate bill for each student using the correct formula:
        // Student Bill = (Student Present Days / Total Present Days of All Students) × Total Monthly Expenses
        for (Student student : students) {
            Integer studentPresentDays = attendanceRepository.countPresentDaysByStudentAndMonth(
                    student, monthYear.getYear(), monthYear.getMonthValue());

            if (studentPresentDays == null) {
                studentPresentDays = 0;
            }

            BigDecimal amountDue = BigDecimal.ZERO;
            if (studentPresentDays > 0 && totalPresentDaysAllStudents > 0) {
                // Calculate proportional amount: (student days / total days) * total expense
                BigDecimal proportion = new BigDecimal(studentPresentDays)
                        .divide(new BigDecimal(totalPresentDaysAllStudents), 6, RoundingMode.HALF_UP);
                amountDue = totalAmount.multiply(proportion).setScale(2, RoundingMode.HALF_UP);
            }

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

    @Override
    @Transactional
    public void recordPayment(PaymentRequest paymentRequest, String adminEmail) {
        // Validate input
        if (paymentRequest == null) {
            throw new BusinessException("Payment request is required");
        }

        if (paymentRequest.getStudentId() == null) {
            throw new BusinessException("Student ID is required");
        }

        if (paymentRequest.getAmount() == null || paymentRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be greater than zero");
        }

        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            throw new BusinessException("Admin email is required");
        }

        Student student = studentRepository.findById(paymentRequest.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        User admin = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException("Only admins can record payments");
        }

        // Add funds to student balance
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
