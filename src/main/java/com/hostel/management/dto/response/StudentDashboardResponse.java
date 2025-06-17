package com.hostel.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StudentDashboardResponse {
    private BigDecimal currentBalance;
    private BigDecimal pendingBillAmount;
    private BigDecimal netBalance; // New field: positive = advance, negative = dues
    private BigDecimal monthlyExpenses;
    private Integer presentDaysThisMonth;
    private Integer totalDaysThisMonth;
    private Boolean isMonitor;
    private String fullName;
}
