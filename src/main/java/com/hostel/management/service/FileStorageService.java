package com.hostel.management.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for file storage operations
 */
public interface FileStorageService {
    /**
     * Stores a file and returns the filename
     *
     * @param file The file to store
     * @return The stored filename
     */
    String storeFile(MultipartFile file);
}
