package com.smartcampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassAttendanceSummaryResponse {

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private LocalDate attendanceDate;
    private long totalEnrolled;
    private long totalPresent;
    private long totalAbsent;
    private long totalOnLeave;
    private double attendancePercentage;
}
