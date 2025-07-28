package com.hostel.management.controller;

import com.hostel.management.dto.response.ApiResponse;
import com.hostel.management.entity.AbsenceRequest;
import com.hostel.management.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    @GetMapping("/absence-requests/early")
    @PreAuthorize("@userServiceImpl.isMonitor(authentication.name)")
    public ResponseEntity<ApiResponse<List<AbsenceRequest>>> getEarlyAbsenceRequests() {
        List<AbsenceRequest> requests = monitorService.getEarlyAbsenceRequests();
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @PutMapping("/absence-requests/{id}/approve")
    @PreAuthorize("@userServiceImpl.isMonitor(authentication.name)")
    public ResponseEntity<ApiResponse<String>> approveAbsenceRequest(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            Authentication authentication) {

        monitorService.approveAbsenceRequest(id, comments, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Absence request approved"));
    }

    @PutMapping("/absence-requests/{id}/reject")
    @PreAuthorize("@userServiceImpl.isMonitor(authentication.name)")
    public ResponseEntity<ApiResponse<String>> rejectAbsenceRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication authentication) {

        monitorService.rejectAbsenceRequest(id, reason, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Absence request rejected"));
    }
}
