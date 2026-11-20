package com.smartcampus.service;

import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.request.BatchAttendanceRequest;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.ClassAttendanceSummaryResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<AttendanceResponse> markBatchAttendance(Long facultyUserId, BatchAttendanceRequest request);

    AttendanceResponse updateAttendance(Long attendanceId, AttendanceUpdateRequest request);

    List<AttendanceResponse> getAttendanceBySubjectAndDate(Long subjectId, LocalDate date);

    StudentAttendanceSummaryResponse getStudentAttendanceSummary(Long studentId);

    ClassAttendanceSummaryResponse getClassAttendanceSummary(Long subjectId, LocalDate date);

    PagedResponse<AttendanceResponse> getStudentAttendanceHistory(Long studentId, int page, int size);
}
