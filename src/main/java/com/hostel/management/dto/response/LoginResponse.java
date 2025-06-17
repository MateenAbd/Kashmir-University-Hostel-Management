package com.hostel.management.dto.response;

import com.hostel.management.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String email;
    private User.UserRole role;
    private Boolean isMonitor;
    private String fullName;
}
