package com.hostel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentListResponse {
    private Long studentId;
    private String enrollmentNo;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private String batch;
    private String district;
    private Boolean isMonitor;
    private BigDecimal currentBalance;
    private LocalDateTime createdAt;
}
