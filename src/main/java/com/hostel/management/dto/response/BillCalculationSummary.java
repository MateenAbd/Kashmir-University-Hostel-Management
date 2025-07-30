package com.hostel.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BillCalculationSummary {
    private String monthYear;
    private BigDecimal totalExpense;
    private Integer totalStudents;
    private Integer totalPresentDays;
    private BigDecimal totalBillAmount;
    private BigDecimal averageBillAmount;
    private Boolean isCalculationValid;

}