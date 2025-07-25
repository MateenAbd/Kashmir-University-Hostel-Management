package com.hostel.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class RegistrationRequestResponse {
    private Long requestId;
    private String formNumber;
    private String email;
    private String enrollmentNo;
    private String fullName;
    private String phone;
    private String department;
    private String batch;
    private String pincode;
    private String district;
    private String tehsil;
    private String guardianPhone;
    private String photoUrl;
    private String status;
    private String comments;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
