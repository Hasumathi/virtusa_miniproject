package com.smartcampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterGradeSummaryResponse {

    private Long studentId;
    private String studentName;
    private String registerNumber;
    private Integer semester;
    private String academicYear;
    private List<GradeResponse> grades;
    private double overallPercentage;
    private int totalSubjects;
    private int passedSubjects;
    private int failedSubjects;
    private String semesterStatus; // PASS, FAIL
}
