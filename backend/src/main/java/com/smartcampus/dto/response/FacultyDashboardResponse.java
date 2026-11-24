package com.smartcampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDashboardResponse {

    private long totalStudents;
    private long presentToday;
    private long absentToday;
    private double overallAttendancePercentage;
    private double averageAcademicPerformance;
    private Map<String, Long> departmentWiseStudentCount;
    private List<StudentResponse> recentlyAddedStudents;
}
