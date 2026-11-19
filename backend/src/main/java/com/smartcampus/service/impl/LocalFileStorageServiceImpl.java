package com.smartcampus.service.impl;

import com.smartcampus.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);

    private final Path fileStorageLocation;
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/jpg"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public LocalFileStorageServiceImpl(@Value("${app.upload.dir:uploads/profile-images}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored: {}", ex.getMessage());
            throw new RuntimeException("Could not create storage directory.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 5MB.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid image format. Allowed formats: JPEG, PNG, WEBP.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Prevent path traversal attacks
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence " + originalFilename);
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File successfully stored: {}", uniqueFilename);
            return uniqueFilename;
        } catch (IOException ex) {
            log.error("Could not store file {}: {}", uniqueFilename, ex.getMessage());
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    @Override
    public byte[] loadFileAsBytes(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new IllegalArgumentException("Unauthorized path traversal attempt");
            }
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + fileName);
            }
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Error reading file: " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (filePath.startsWith(this.fileStorageLocation)) {
                Files.deleteIfExists(filePath);
                log.info("Deleted file: {}", fileName);
            }
        } catch (IOException ex) {
            log.warn("Could not delete file {}: {}", fileName, ex.getMessage());
        }
    }
}
