package com.hostel.management.service.impl;

import com.hostel.management.entity.*;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.repository.BillRepository;
import com.hostel.management.repository.PaymentRepository;
import com.hostel.management.repository.StudentRepository;
import com.hostel.management.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {
    private final StudentRepository studentRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public BigDecimal addFundsToStudentBalance(Student student, BigDecimal amount) {
        // Validate input
        if (student == null) {
            throw new BusinessException("Student is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Amount must be greater than zero");
        }

        // Validate amount is reasonable (not more than 1 million)
        if (amount.compareTo(new BigDecimal("1000000")) > 0) {
            throw new BusinessException("Amount cannot exceed 1,000,000");
        }

        // Add to student's current balance
        BigDecimal currentBalance = student.getCurrentBalance() != null ?
                student.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(amount);

        student.setCurrentBalance(newBalance);
        studentRepository.save(student);

        log.info("Added {} to student {} balance. New balance: {}",
                amount, student.getStudentId(), newBalance);

        return newBalance;
    }

    @Override
    public BigDecimal getTotalPendingDues(Student student) {
        if (student == null) {
            return BigDecimal.ZERO;
        }

        List<Bill> pendingBills = billRepository.findByStudentAndStatusNotOrderByMonthYearAsc(
                student, Bill.BillStatus.FULLY_PAID);

        BigDecimal totalDue = BigDecimal.ZERO;
        for (Bill bill : pendingBills) {
            BigDecimal amountDue = bill.getAmountDue() != null ? bill.getAmountDue() : BigDecimal.ZERO;
            BigDecimal amountPaid = bill.getAmountPaid() != null ? bill.getAmountPaid() : BigDecimal.ZERO;
            BigDecimal remaining = amountDue.subtract(amountPaid);

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                totalDue = totalDue.add(remaining);
            }
        }

        return totalDue;
    }

    @Override
    public BigDecimal getNetBalance(Student student) {
        if (student == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal currentBalance = student.getCurrentBalance() != null ?
                student.getCurrentBalance() : BigDecimal.ZERO;
        BigDecimal pendingDues = getTotalPendingDues(student);

        return currentBalance.subtract(pendingDues);
    }
}
