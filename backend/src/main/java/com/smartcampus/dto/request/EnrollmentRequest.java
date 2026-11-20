package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequest {

    @NotNull(message = "Student ID is mandatory")
    private Long studentId;

    @NotNull(message = "Subject ID is mandatory")
    private Long subjectId;

    @NotBlank(message = "Academic year is mandatory (e.g. 2025-2026)")
    private String academicYear;

    @NotNull(message = "Semester is mandatory")
    private Integer semester;
}
