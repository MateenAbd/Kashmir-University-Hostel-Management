package com.hostel.management.service;

/**
 * Service interface for attendance operations
 */
public interface AttendanceService {
    /**
     * Creates daily attendance records for all students
     * This method is scheduled to run at 12:01 AM every day
     */
    void createDailyAttendanceRecords();
}
