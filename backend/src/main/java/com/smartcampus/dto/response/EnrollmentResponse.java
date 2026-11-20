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
public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentRegisterNumber;
    private String studentName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String academicYear;
    private Integer semester;
    private LocalDateTime enrolledAt;
}
