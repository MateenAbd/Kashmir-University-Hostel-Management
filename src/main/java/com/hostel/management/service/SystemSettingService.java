package com.hostel.management.service;

import com.hostel.management.entity.SystemSetting;

import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for system settings operations
 */
public interface SystemSettingService {
    /**
     * Gets the absence request cutoff time
     *
     * @return LocalTime representing the cutoff time
     */
    LocalTime getAbsenceRequestCutoffTime();

    /**
     * Updates the absence request cutoff time
     *
     * @param cutoffTime New cutoff time
     * @param wardenEmail Email of the warden updating the setting
     */
    void updateAbsenceRequestCutoffTime(LocalTime cutoffTime, String wardenEmail);

    /**
     * Gets all system settings
     *
     * @return List of all system settings
     */
    List<SystemSetting> getAllSettings();

    /**
     * Gets a setting by its key
     *
     * @param key Setting key
     * @return SystemSetting entity
     */
    SystemSetting getSettingByKey(String key);
}
