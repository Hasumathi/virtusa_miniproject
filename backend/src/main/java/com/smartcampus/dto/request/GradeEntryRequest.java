package com.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeEntryRequest {

    @NotNull(message = "Student ID is mandatory")
    private Long studentId;

    @NotNull(message = "Subject ID is mandatory")
    private Long subjectId;

    @NotNull(message = "Semester is mandatory")
    @Min(1) @Max(10)
    private Integer semester;

    @NotBlank(message = "Academic year is mandatory")
    private String academicYear;

    @Min(value = 0, message = "Internal marks cannot be negative")
    @Max(value = 25, message = "Internal marks maximum is 25")
    private Double internalMarks;

    @Min(value = 0, message = "Assignment marks cannot be negative")
    @Max(value = 15, message = "Assignment marks maximum is 15")
    private Double assignmentMarks;

    @Min(value = 0, message = "Practical marks cannot be negative")
    @Max(value = 20, message = "Practical marks maximum is 20")
    private Double practicalMarks;

    @NotNull(message = "Exam marks are mandatory")
    @Min(value = 0, message = "Exam marks cannot be negative")
    @Max(value = 40, message = "Exam marks maximum is 40")
    private Double examMarks;

    @Builder.Default
    private Boolean isPublished = true;
}
