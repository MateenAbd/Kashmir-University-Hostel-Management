package com.hostel.management.service.impl;

import com.hostel.management.entity.SystemSetting;
import com.hostel.management.entity.User;
import com.hostel.management.exception.BusinessException;
import com.hostel.management.exception.ResourceNotFoundException;
import com.hostel.management.repository.SystemSettingRepository;
import com.hostel.management.repository.UserRepository;
import com.hostel.management.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {
    private final SystemSettingRepository systemSettingRepository;
    private final UserRepository userRepository;

    @Override
    public LocalTime getAbsenceRequestCutoffTime() {
        SystemSetting setting = systemSettingRepository.findBySettingKey(SystemSetting.ABSENCE_REQUEST_CUTOFF_TIME)
                .orElse(null);

        if (setting == null) {
            // Return default time if not configured
            return LocalTime.of(11, 0);
        }

        try {
            return LocalTime.parse(setting.getSettingValue(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            // Return default time if parsing fails
            return LocalTime.of(11, 0);
        }
    }

    @Override
    @Transactional
    public void updateAbsenceRequestCutoffTime(LocalTime cutoffTime, String wardenEmail) {
        // Validate input
        if (cutoffTime == null) {
            throw new BusinessException("Cutoff time cannot be null");
        }

        if (wardenEmail == null || wardenEmail.trim().isEmpty()) {
            throw new BusinessException("Warden email is required");
        }

        User warden = userRepository.findByEmail(wardenEmail.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Warden not found"));

        if (warden.getRole() != User.UserRole.WARDEN) {
            throw new BusinessException("Only wardens can update system settings");
        }

        // Validate time range (should be reasonable business hours)
        if (cutoffTime.isBefore(LocalTime.of(6, 0)) || cutoffTime.isAfter(LocalTime.of(23, 59))) {
            throw new BusinessException("Cutoff time must be between 06:00 and 23:59");
        }

        String timeValue = cutoffTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        SystemSetting setting = systemSettingRepository.findBySettingKey(SystemSetting.ABSENCE_REQUEST_CUTOFF_TIME)
                .orElse(SystemSetting.builder()
                        .settingKey(SystemSetting.ABSENCE_REQUEST_CUTOFF_TIME)
                        .description("Cutoff time for early vs late absence requests (HH:mm format)")
                        .build());

        setting.setSettingValue(timeValue);
        setting.setUpdatedBy(warden);

        systemSettingRepository.save(setting);
    }

    @Override
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    @Override
    public SystemSetting getSettingByKey(String key) {
        return systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key));
    }
}
