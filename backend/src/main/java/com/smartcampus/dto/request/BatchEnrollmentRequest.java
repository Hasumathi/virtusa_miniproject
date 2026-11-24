package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchEnrollmentRequest {

    @NotNull(message = "Subject ID is mandatory")
    private Long subjectId;

    @NotEmpty(message = "List of student IDs must not be empty")
    private List<Long> studentIds;

    @NotBlank(message = "Academic year is mandatory")
    private String academicYear;

    @NotNull(message = "Semester is mandatory")
    private Integer semester;
}
