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
public class GradeResponse {

    private Long id;
    private Long studentId;
    private String studentRegisterNumber;
    private String studentName;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Integer semester;
    private String academicYear;
    private Double internalMarks;
    private Double assignmentMarks;
    private Double practicalMarks;
    private Double examMarks;
    private Double totalMarks;
    private Double percentage;
    private String letterGrade;
    private Boolean isPassed;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
