package com.hostel.management.service;

import com.hostel.management.entity.*;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.repository.BillRepository;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    private final StudentRepository studentRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    /**
     * Adds funds to a student's balance
     *
     * @param student The student receiving the payment
     * @param amount The amount being added
     * @return The new balance after adding funds
     */
    @Transactional
    public BigDecimal addFundsToStudentBalance(Student student, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        // Add to student's current balance
        BigDecimal currentBalance = student.getCurrentBalance();
        BigDecimal newBalance = currentBalance.add(amount);
        student.setCurrentBalance(newBalance);
        studentRepository.save(student);

        log.info("Added {} to student {} balance. New balance: {}",
                amount, student.getStudentId(), newBalance);

        return newBalance;
    }

    /**
     * Gets the total pending dues for a student across all bills
     *
     * @param student The student to check
     * @return The total amount due
     */
    public BigDecimal getTotalPendingDues(Student student) {
        List<Bill> pendingBills = billRepository.findByStudentAndStatusNotOrderByMonthYearAsc(
                student, Bill.BillStatus.FULLY_PAID);

        BigDecimal totalDue = BigDecimal.ZERO;
        for (Bill bill : pendingBills) {
            totalDue = totalDue.add(bill.getAmountDue().subtract(bill.getAmountPaid()));
        }

        return totalDue;
    }

    /**
     * Calculates the net balance for a student (current balance - pending dues)
     * This shows if the student has advance payment or owes money
     *
     * @param student The student to check
     * @return The net balance (positive = advance payment, negative = owes money)
     */
    public BigDecimal getNetBalance(Student student) {
        BigDecimal currentBalance = student.getCurrentBalance();
        BigDecimal pendingDues = getTotalPendingDues(student);

        return currentBalance.subtract(pendingDues);
    }
}
