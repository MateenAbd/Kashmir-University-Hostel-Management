package com.hostel.management.service;

import com.hostel.management.entity.Student;

import java.math.BigDecimal;

/**
 * Service interface for billing operations
 */
public interface BillingService {
    /**
     * Adds funds to a student's balance
     *
     * @param student The student receiving the payment
     * @param amount The amount being added
     * @return The new balance after adding funds
     */
    BigDecimal addFundsToStudentBalance(Student student, BigDecimal amount);

    /**
     * Gets the total pending dues for a student across all bills
     *
     * @param student The student to check
     * @return The total amount due
     */
    BigDecimal getTotalPendingDues(Student student);

    /**
     * Calculates the net balance for a student (current balance - pending dues)
     * This shows if the student has advance payment or owes money
     *
     * @param student The student to check
     * @return The net balance (positive = advance payment, negative = owes money)
     */
    BigDecimal getNetBalance(Student student);
}
