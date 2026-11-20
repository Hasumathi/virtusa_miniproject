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
public class StudentAttendanceSummaryResponse {

    private Long studentId;
    private String registerNumber;
    private String studentName;
    private long totalSessions;
    private long totalPresent;
    private long totalAbsent;
    private long totalOnLeave;
    private double overallPercentage;
    private boolean belowThreshold;
    private double lowThresholdPercentage;
    private List<SubjectAttendanceDetail> subjectDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubjectAttendanceDetail {
        private Long subjectId;
        private String subjectCode;
        private String subjectName;
        private long totalClasses;
        private long attendedClasses;
        private long absentClasses;
        private long onLeaveClasses;
        private double percentage;
        private boolean belowThreshold;
    }
}
