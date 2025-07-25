package com.hostel.management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SystemSettingRequest {
    @NotNull(message = "Cutoff time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Time must be in HH:mm format (24-hour)")
    private String cutoffTime;
}
