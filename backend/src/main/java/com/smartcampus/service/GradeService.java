package com.smartcampus.service;

import com.smartcampus.dto.request.GradeEntryRequest;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.dto.response.SemesterGradeSummaryResponse;

import java.util.List;

public interface GradeService {

    GradeResponse enterOrUpdateGrade(GradeEntryRequest request);

    GradeResponse getGradeById(Long gradeId);

    List<GradeResponse> getGradesBySubjectAndSemester(Long subjectId, Integer semester);

    List<GradeResponse> getStudentGrades(Long studentId);

    SemesterGradeSummaryResponse getStudentSemesterGrades(Long studentId, Integer semester);

    void publishGrades(Long subjectId, Integer semester, String academicYear);
}
