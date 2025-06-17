package com.hostel.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class StudentRegistrationRequest {
    @NotBlank(message = "Form number is required")
    private String formNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Enrollment number is required")
    private String enrollmentNo;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Batch is required")
    private String batch;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Tehsil is required")
    private String tehsil;

    @NotBlank(message = "Guardian phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Guardian phone must be 10 digits")
    private String guardianPhone;

    @NotNull(message = "Photo is required")
    private MultipartFile photo;
}
