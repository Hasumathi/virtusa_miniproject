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
public class StudentDashboardResponse {

    private Long studentId;
    private String studentName;
    private String registerNumber;
    private Integer currentSemester;
    private String departmentName;
    private double overallAttendancePercentage;
    private long totalEnrolledSubjects;
    private double averageMarks;
    private List<GradeResponse> recentGrades;
    private long unreadNotificationsCount;
}
