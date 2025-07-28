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
    private String calculationFormula;
    
    public BillCalculationSummary(String monthYear, BigDecimal totalExpense, Integer totalStudents, 
                                Integer totalPresentDays, BigDecimal totalBillAmount, 
                                BigDecimal averageBillAmount, Boolean isCalculationValid, 
                                String calculationFormula) {
        this.monthYear = monthYear;
        this.totalExpense = totalExpense;
        this.totalStudents = totalStudents;
        this.totalPresentDays = totalPresentDays;
        this.totalBillAmount = totalBillAmount;
        this.averageBillAmount = averageBillAmount;
        this.isCalculationValid = isCalculationValid;
        this.calculationFormula = "Student Bill = (Student Present Days ÷ Total Present Days of All Students) × Total Monthly Expenses";
    }
}
