package com.hostel.management.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemSettingResponse {
    private Long settingId;
    private String settingKey;
    private String settingValue;
    private String description;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
