package com.hostel.management.controller;

import com.hostel.management.dto.request.SystemSettingRequest;
import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.dto.response.SystemSettingResponse;
import com.hostel.management.entity.AbsenceRequest;
import com.hostel.management.entity.DeletionRequest;
import com.hostel.management.entity.MonthlyExpense;
import com.hostel.management.entity.SystemSetting;
import com.hostel.management.service.SystemSettingService;
import com.hostel.management.service.WardenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/warden")
@RequiredArgsConstructor
public class WardenController {

    private final WardenService wardenService;
    private final SystemSettingService systemSettingService;

    @GetMapping("/deletion-requests")
    public ResponseEntity<ApiResponse<List<DeletionRequest>>> getPendingDeletionRequests() {
        List<DeletionRequest> requests = wardenService.getPendingDeletionRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PutMapping("/deletion-requests/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveDeletionRequest(
            @PathVariable Long id,
            Authentication authentication) {

        wardenService.approveDeletionRequest(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Student deletion request approved"));
    }

    @PutMapping("/deletion-requests/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectDeletionRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {

        wardenService.rejectDeletionRequest(id, reason, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Student deletion request rejected"));
    }

    @GetMapping("/expenses")
    public ResponseEntity<ApiResponse<List<MonthlyExpense>>> getMonthlyExpenses() {
        List<MonthlyExpense> expenses = wardenService.getAllMonthlyExpenses();
        return ResponseEntity.ok(ApiResponse.success(expenses));
    }

    @GetMapping("/expenses/{monthYear}")
    public ResponseEntity<ApiResponse<MonthlyExpense>> getMonthlyExpense(
            @PathVariable String monthYear) {
        MonthlyExpense expense = wardenService.getMonthlyExpense(monthYear);
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    @GetMapping("/absence-requests/late")
    public ResponseEntity<ApiResponse<List<AbsenceRequest>>> getLateAbsenceRequests() {
        List<AbsenceRequest> requests = wardenService.getLateAbsenceRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PutMapping("/absence-requests/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveAbsenceRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            Authentication authentication) {

        wardenService.approveAbsenceRequest(id, comments, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Absence request approved"));
    }

    @PutMapping("/absence-requests/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectAbsenceRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {

        wardenService.rejectAbsenceRequest(id, reason, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Absence request rejected"));
    }

    // System settings endpoints
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getSystemSettings() {
        List<SystemSetting> settings = systemSettingService.getAllSettings();
        List<SystemSettingResponse> responses = settings.stream()
                .map(setting -> SystemSettingResponse.builder()
                        .settingId(setting.getSettingId())
                        .settingKey(setting.getSettingKey())
                        .settingValue(setting.getSettingValue())
                        .description(setting.getDescription())
                        .updatedBy(setting.getUpdatedBy() != null ? setting.getUpdatedBy().getEmail() : null)
                        .updatedAt(setting.getUpdatedAt())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/settings/absence-cutoff-time")
    public ResponseEntity<ApiResponse<String>> getAbsenceRequestCutoffTime() {
        LocalTime cutoffTime = systemSettingService.getAbsenceRequestCutoffTime();
        String timeString = cutoffTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        return ResponseEntity.ok(ApiResponse.success("Current cutoff time retrieved", timeString));
    }

    @PutMapping("/settings/absence-cutoff-time")
    public ResponseEntity<ApiResponse<String>> updateAbsenceRequestCutoffTime(
            @Valid @RequestBody SystemSettingRequest request,
            Authentication authentication) {

        LocalTime cutoffTime = LocalTime.parse(request.getCutoffTime(), DateTimeFormatter.ofPattern("HH:mm"));
        systemSettingService.updateAbsenceRequestCutoffTime(cutoffTime, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Absence request cutoff time updated successfully"));
    }
}
