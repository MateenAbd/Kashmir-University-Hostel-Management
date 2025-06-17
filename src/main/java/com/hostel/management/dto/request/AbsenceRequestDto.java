package com.hostel.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AbsenceRequestDto {
    @NotNull(message = "Absence date is required")
    private LocalDate absenceDate;

    @NotBlank(message = "Reason is required")
    @Size(min = 10, message = "Reason must be at least 10 characters")
    private String reason;
}
