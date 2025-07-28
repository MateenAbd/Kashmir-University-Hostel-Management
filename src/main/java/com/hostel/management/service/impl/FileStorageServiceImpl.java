package com.hostel.management.service.impl;

import com.hostel.management.exception.FileStorageException;
import com.hostel.management.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public String storeFile(MultipartFile file) {
        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                throw new FileStorageException("Cannot store empty file");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/jpeg") &&
                    !contentType.equals("image/png") &&
                    !contentType.equals("image/jpg"))) {
                throw new FileStorageException("Only JPEG and PNG files are allowed");
            }

            // Validate file size (2MB max)
            long maxSize = 2 * 1024 * 1024; // 2MB
            if (file.getSize() > maxSize) {
                throw new FileStorageException("File size cannot exceed 2MB");
            }

            // Validate filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new FileStorageException("Invalid filename");
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String fileExtension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
                fileExtension = originalFilename.substring(lastDotIndex);
            }

            String filename = UUID.randomUUID().toString() + fileExtension;

            // Store file
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return filename;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file. Please try again!", ex);
        }
    }
}
