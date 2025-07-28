package com.hostel.management.service;

import com.hostel.management.entity.User;

/**
 * Service interface for user-related operations
 */
public interface UserService {
    /**
     * Checks if a user is a monitor
     *
     * @param email User's email
     * @return true if user is a monitor, false otherwise
     */
    boolean isMonitor(String email);

    /**
     * Finds a user by email
     *
     * @param email User's email
     * @return User entity
     */
    User findByEmail(String email);
}
