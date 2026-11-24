package com.smartcampus.service;

import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    byte[] generateStudentAttendancePdf(Long studentId);

    byte[] generateClassAttendancePdf(Long subjectId, LocalDate date);

    byte[] generateSubjectGradePdf(Long subjectId, Integer semester, String academicYear);

    byte[] generateStudentPerformancePdf(Long studentId, Integer semester);

    List<StudentAttendanceSummaryResponse> getStudentsBelowThreshold(Long departmentId);
}
