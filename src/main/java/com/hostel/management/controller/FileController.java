package com.hostel.management.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/admin")
public class FileController {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping("/files/student-photo/{filename:.+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> getStudentPhoto(@PathVariable String filename) {
        try {
            // Resolve and normalize path to prevent traversal
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            if (!filePath.startsWith(Paths.get(uploadDir))) {
                // Prevent path traversal
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // Probe content type accurately
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream"; // Fallback
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache for 1 hour
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            // Log the error (use SLF4J or similar)
            return ResponseEntity.status(500).build(); // Internal server error
        }
    }
}
