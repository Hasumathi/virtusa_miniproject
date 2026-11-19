package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.Student;
import com.smartcampus.enums.Role;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.security.SecurityUtils;
import com.smartcampus.security.UserPrincipal;
import com.smartcampus.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/files")
@Tag(name = "File Management", description = "Endpoints for uploading and retrieving profile images and files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;
    private final SecurityUtils securityUtils;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public FileController(
            FileStorageService fileStorageService,
            SecurityUtils securityUtils,
            StudentRepository studentRepository,
            FacultyRepository facultyRepository) {
        this.fileStorageService = fileStorageService;
        this.securityUtils = securityUtils;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    @PostMapping(value = "/upload/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Upload profile picture for authenticated user")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {

        log.info("Receiving profile image upload: name={}, size={}", file.getOriginalFilename(), file.getSize());
        String storedFileName = fileStorageService.storeFile(file);
        String imageUrl = "/api/files/profile-images/" + storedFileName;

        Optional<UserPrincipal> currentUser = securityUtils.getCurrentUser();
        if (currentUser.isPresent()) {
            UserPrincipal principal = currentUser.get();
            if (principal.getRole() == Role.STUDENT) {
                studentRepository.findByUserId(principal.getId()).ifPresent(student -> {
                    student.setProfileImageUrl(imageUrl);
                    studentRepository.save(student);
                });
            } else if (principal.getRole() == Role.FACULTY) {
                facultyRepository.findByUserId(principal.getId()).ifPresent(faculty -> {
                    faculty.setProfileImageUrl(imageUrl);
                    facultyRepository.save(faculty);
                });
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("fileName", storedFileName);
        response.put("imageUrl", imageUrl);

        return ResponseEntity.ok(ApiResponse.success("Profile image uploaded successfully", response));
    }

    @GetMapping("/profile-images/{fileName:.+}")
    @Operation(summary = "Download or display profile image by filename")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String fileName) {
        byte[] fileBytes = fileStorageService.loadFileAsBytes(fileName);

        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (fileName.toLowerCase().endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (fileName.toLowerCase().endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(fileBytes);
    }
}
