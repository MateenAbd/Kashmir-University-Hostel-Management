package com.hostel.management.service.impl;

import com.hostel.management.dto.request.AbsenceRequestDto;
import com.hostel.management.dto.request.StudentRegistrationRequest;
import com.hostel.management.dto.response.StudentDashboardResponse;
import com.hostel.management.entity.*;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.*;
import com.hostel.management.service.BillingService;
import com.hostel.management.service.FileStorageService;
import com.hostel.management.service.StudentService;
import com.hostel.management.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FormNumberRepository formNumberRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final AttendanceRepository attendanceRepository;
    private final AbsenceRequestRepository absenceRequestRepository;
    private final BillRepository billRepository;
    private final MonthlyExpenseRepository monthlyExpenseRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final BillingService billingService;
    private final SystemSettingService systemSettingService;

    @Override
    @Transactional
    public void registerStudent(StudentRegistrationRequest request) {
        // Validate form number
        FormNumber formNumber = formNumberRepository.findByFormNumberAndIsUsedFalse(request.getFormNumber())
                .orElseThrow(() -> new BusinessException("Invalid or already used form number"));

        // Check for duplicate enrollment number and email
        if (registrationRequestRepository.existsByEnrollmentNo(request.getEnrollmentNo())) {
            throw new BusinessException("Enrollment number already exists");
        }

        if (registrationRequestRepository.existsByEmail(request.getEmail()) ||
                userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists");
        }

        // Upload photo
        String photoUrl = fileStorageService.storeFile(request.getPhoto());

        // Create registration request
        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .formNumber(formNumber)
                .email(request.getEmail())
                .enrollmentNo(request.getEnrollmentNo())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .batch(request.getBatch())
                .pincode(request.getPincode())
                .district(request.getDistrict())
                .tehsil(request.getTehsil())
                .guardianPhone(request.getGuardianPhone())
                .photoUrl(photoUrl)
                .build();

        registrationRequestRepository.save(registrationRequest);

        // Mark form number as used
        formNumber.setIsUsed(true);
        formNumberRepository.save(formNumber);
    }

    @Override
    @Transactional
    public void submitAbsenceRequest(String email, AbsenceRequestDto request) {
        // Validate input
        if (email == null || request == null || request.getAbsenceDate() == null || request.getReason() == null) {
            throw new BusinessException("Invalid absence request data");
        }

        Student student = studentRepository.findByUserEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Check if request already exists for the date
        if (absenceRequestRepository.findByStudentAndAbsenceDate(student, request.getAbsenceDate()).isPresent()) {
            throw new BusinessException("Absence request already exists for this date");
        }

        // Check if the date is not in the past (except today)
        if (request.getAbsenceDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot request absence for past dates");
        }

        // Check if the date is too far in the future (e.g., more than 30 days)
        if (request.getAbsenceDate().isAfter(LocalDate.now().plusDays(30))) {
            throw new BusinessException("Cannot request absence more than 30 days in advance");
        }

        LocalDateTime submissionTime = LocalDateTime.now();

        AbsenceRequest absenceRequest = AbsenceRequest.builder()
                .student(student)
                .requestDate(LocalDate.now())
                .absenceDate(request.getAbsenceDate())
                .reason(request.getReason().trim())
                .submittedAt(submissionTime)
                .build();

        // Set late request status based on configurable cutoff time
        LocalTime cutoffTime = systemSettingService.getAbsenceRequestCutoffTime();
        absenceRequest.setLateRequestStatus(cutoffTime);

        absenceRequestRepository.save(absenceRequest);
    }

    @Override
    public List<Attendance> getAttendanceHistory(String email, int months) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        return attendanceRepository.findByStudentAndDateBetween(student, startDate, endDate);
    }

    @Override
    public StudentDashboardResponse getStudentDashboard(String email) {
        Student student = studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        YearMonth currentMonth = YearMonth.now();
        String monthYear = currentMonth.toString();

        // Get current month bill
        Bill currentBill = billRepository.findByStudentAndMonthYear(student, monthYear).orElse(null);
        BigDecimal pendingBillAmount = currentBill != null ?
                currentBill.getAmountDue().subtract(currentBill.getAmountPaid()) : BigDecimal.ZERO;

        // Get monthly expenses
        MonthlyExpense monthlyExpense = monthlyExpenseRepository.findByMonthYear(monthYear).orElse(null);
        BigDecimal monthlyExpenses = monthlyExpense != null ? monthlyExpense.getTotalAmount() : BigDecimal.ZERO;

        // Get attendance for current month
        Integer presentDays = attendanceRepository.countPresentDaysByStudentAndMonth(
                student, currentMonth.getYear(), currentMonth.getMonthValue());

        // Calculate net balance (positive = advance payment, negative = owes money)
        BigDecimal netBalance = billingService.getNetBalance(student);

        return StudentDashboardResponse.builder()
                .currentBalance(student.getCurrentBalance())
                .pendingBillAmount(pendingBillAmount)
                .netBalance(netBalance)
                .monthlyExpenses(monthlyExpenses)
                .presentDaysThisMonth(presentDays != null ? presentDays : 0)
                .totalDaysThisMonth(currentMonth.lengthOfMonth())
                .isMonitor(student.getIsMonitor())
                .fullName(student.getFullName())
                .build();
    }

    @Override
    public Student findByEmail(String email) {
        return studentRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }
}
