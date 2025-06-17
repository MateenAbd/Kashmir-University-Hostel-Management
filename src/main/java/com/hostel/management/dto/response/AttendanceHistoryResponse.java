package com.hostel.management.dto.response;

import com.hostel.management.entity.Attendance;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AttendanceHistoryResponse {
    private LocalDate date;
    private Attendance.AttendanceStatus status;
    private String approvedBy;
    private Integer presentDays;
    private Integer totalDays;
}
