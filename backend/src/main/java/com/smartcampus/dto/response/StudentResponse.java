package com.smartcampus.dto.response;

import com.smartcampus.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    private Long id;
    private String registerNumber;
    private String fullName;
    private String email;
    private String phone;
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private Integer year;
    private Integer semester;
    private String section;
    private LocalDate dateOfJoining;
    private String profileImageUrl;
    private AccountStatus accountStatus;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
