package com.smartcampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResponse {

    private Long id;
    private String employeeId;
    private String fullName;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private String designation;
    private String profileImageUrl;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
