package com.hostel.management.service.impl;

import com.hostel.management.dto.response.BillCalculationSummary;
import com.hostel.management.entity.Bill;
import com.hostel.management.entity.MonthlyExpense;
import com.hostel.management.entity.Student;
import com.hostel.management.repository.AttendanceRepository;
import com.hostel.management.repository.BillRepository;
import com.hostel.management.repository.MonthlyExpenseRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.service.BillCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillCalculationServiceImpl implements BillCalculationService {
    
    private final BillRepository billRepository;
    private final MonthlyExpenseRepository monthlyExpenseRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    @Override
    public BigDecimal calculateStudentBill(Student student, YearMonth monthYear, 
                                         BigDecimal totalMonthlyExpense, 
                                         Integer studentPresentDays, 
                                         Integer totalPresentDaysAllStudents) {
        
        // Validate inputs
        if (student == null || monthYear == null || totalMonthlyExpense == null) {
            return BigDecimal.ZERO;
        }
        
        if (studentPresentDays == null || studentPresentDays <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (totalPresentDaysAllStudents == null || totalPresentDaysAllStudents <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (totalMonthlyExpense.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Calculate proportional amount using the correct formula:
        // Student Bill = (Student Present Days / Total Present Days of All Students) × Total Monthly Expenses
        BigDecimal proportion = new BigDecimal(studentPresentDays)
                .divide(new BigDecimal(totalPresentDaysAllStudents), 6, RoundingMode.HALF_UP);
        
        BigDecimal calculatedAmount = totalMonthlyExpense.multiply(proportion)
                .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Bill calculation for student {}: {} days / {} total days = {} proportion, " +
                 "Amount: {} × {} = {}", 
                 student.getStudentId(), studentPresentDays, totalPresentDaysAllStudents, 
                 proportion, totalMonthlyExpense, proportion, calculatedAmount);
        
        return calculatedAmount;
    }

    @Override
    public boolean validateBillCalculation(List<Bill> bills, BigDecimal totalMonthlyExpense) {
        if (bills == null || bills.isEmpty()) {
            return totalMonthlyExpense.compareTo(BigDecimal.ZERO) == 0;
        }
        
        BigDecimal sumOfBills = bills.stream()
                .map(Bill::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Allow for small rounding differences (within 1 rupee)
        BigDecimal difference = sumOfBills.subtract(totalMonthlyExpense).abs();
        boolean isValid = difference.compareTo(BigDecimal.ONE) <= 0;
        
        if (!isValid) {
            log.warn("Bill calculation validation failed. Sum of bills: {}, Total expense: {}, Difference: {}", 
                    sumOfBills, totalMonthlyExpense, difference);
        }
        
        return isValid;
    }

    @Override
    public BillCalculationSummary getBillCalculationSummary(YearMonth monthYear) {
        String monthYearStr = monthYear.toString();
        
        // Get monthly expense
        MonthlyExpense expense = monthlyExpenseRepository.findByMonthYear(monthYearStr).orElse(null);
        if (expense == null) {
            return BillCalculationSummary.builder()
                    .monthYear(monthYearStr)
                    .totalExpense(BigDecimal.ZERO)
                    .totalStudents(0)
                    .totalPresentDays(0)
                    .totalBillAmount(BigDecimal.ZERO)
                    .isCalculationValid(false)
                    .build();
        }
        
        // Get all bills for the month
        List<Student> students = studentRepository.findAll();
        List<Bill> bills = students.stream()
                .map(student -> billRepository.findByStudentAndMonthYear(student, monthYearStr).orElse(null))
                .filter(bill -> bill != null)
                .toList();
        
        // Get total present days
        Integer totalPresentDays = attendanceRepository.countTotalPresentDaysByMonth(
                monthYear.getYear(), monthYear.getMonthValue());
        
        // Calculate summary
        BigDecimal totalBillAmount = bills.stream()
                .map(Bill::getAmountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        boolean isValid = validateBillCalculation(bills, expense.getTotalAmount());
        
        return BillCalculationSummary.builder()
                .monthYear(monthYearStr)
                .totalExpense(expense.getTotalAmount())
                .totalStudents(students.size())
                .totalPresentDays(totalPresentDays != null ? totalPresentDays : 0)
                .totalBillAmount(totalBillAmount)
                .isCalculationValid(isValid)
                .averageBillAmount(bills.isEmpty() ? BigDecimal.ZERO : 
                        totalBillAmount.divide(new BigDecimal(bills.size()), 2, RoundingMode.HALF_UP))
                .build();
    }
}
