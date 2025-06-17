package com.hostel.management.controller;

import com.hostel.management.dto.request.PaymentRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.RegistrationRequestResponse;
import com.hostel.management.entity.RegistrationRequest;
import com.hostel.management.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/form-numbers")
    public ResponseEntity<ApiResponse<String>> addFormNumbers(
            @RequestBody List<String> formNumbers,
            Authentication authentication) {

        adminService.addFormNumbers(formNumbers, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Form numbers added successfully"));
    }

    //    @GetMapping("/registration-requests")
//    public ResponseEntity<ApiResponse<List<RegistrationRequest>>> getPendingRequests() {
//        List<RegistrationRequest> requests = adminService.getPendingRegistrationRequests();
//        return ResponseEntity.ok(ApiResponse.success(requests));
//    }
// below code has to be reviewed
    @GetMapping("/registration-requests")
    public ResponseEntity<ApiResponse<List<RegistrationRequestResponse>>> getPendingRequests() {
        List<RegistrationRequestResponse> dtos = adminService.getPendingRegistrationRequestResponses();
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @PutMapping("/registration-requests/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveRequest(
            @PathVariable Long id,
            Authentication authentication) {

        adminService.approveRegistrationRequest(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Registration request approved"));
    }

    @PutMapping("/registration-requests/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectRequest(
            @PathVariable Long id,
            @RequestParam String comments,
            Authentication authentication) {

        adminService.rejectRegistrationRequest(id, comments, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Registration request rejected"));
    }

    @PostMapping("/monitor/{studentId}")
    public ResponseEntity<ApiResponse<String>> assignMonitor(@PathVariable Long studentId) {
        adminService.assignMonitorRole(studentId);
        return ResponseEntity.ok(ApiResponse.success("Monitor role assigned successfully"));
    }

    @PostMapping("/expenses")
    public ResponseEntity<ApiResponse<String>> enterMonthlyExpense(
            @RequestParam YearMonth monthYear,
            @RequestParam BigDecimal totalAmount,
            Authentication authentication) {

        adminService.enterMonthlyExpense(monthYear, totalAmount, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Monthly expense entered and bills generated"));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<String>> recordPayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        adminService.recordPayment(request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully"));
    }

    @PostMapping("/deletion-request/{studentId}")
    public ResponseEntity<ApiResponse<String>> requestStudentDeletion(
            @PathVariable Long studentId,
            @RequestParam String reason,
            Authentication authentication) {

        adminService.requestStudentDeletion(studentId, reason, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Student deletion request submitted"));
    }
}
