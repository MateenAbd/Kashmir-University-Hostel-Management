package com.hostel.management.service;

import com.hostel.management.dto.response.BillCalculationSummary;
import com.hostel.management.entity.Bill;
import com.hostel.management.entity.Student;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

/**
 * Service interface for bill calculation operations
 */
public interface BillCalculationService {
    /**
     * Calculates the bill amount for a student based on proportional attendance
     * Formula: (Student Present Days / Total Present Days of All Students) × Total Monthly Expenses
     *
     * @param student The student for whom to calculate the bill
     * @param monthYear The month and year for the calculation
     * @param totalMonthlyExpense The total monthly expense to be distributed
     * @param studentPresentDays Number of days the student was present
     * @param totalPresentDaysAllStudents Total present days of all students combined
     * @return The calculated bill amount for the student
     */
    BigDecimal calculateStudentBill(Student student, YearMonth monthYear,
                                    BigDecimal totalMonthlyExpense,
                                    Integer studentPresentDays,
                                    Integer totalPresentDaysAllStudents);

    /**
     * Validates that the sum of all student bills equals the total monthly expense
     *
     * @param bills List of bills for the month
     * @param totalMonthlyExpense The total monthly expense
     * @return true if the calculation is correct, false otherwise
     */
    boolean validateBillCalculation(List<Bill> bills, BigDecimal totalMonthlyExpense);

    /**
     * Gets a breakdown of bill calculation for a specific month
     *
     * @param monthYear The month and year
     * @return Bill calculation summary
     */
    BillCalculationSummary getBillCalculationSummary(YearMonth monthYear);
}
