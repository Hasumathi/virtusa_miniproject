package com.smartcampus.service;

import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.dto.response.SemesterGradeSummaryResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse.SubjectAttendanceDetail;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.Subject;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private GradeService gradeService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Student sampleStudent;
    private Subject sampleSubject;
    private Department sampleDepartment;

    @BeforeEach
    void setUp() {
        sampleDepartment = Department.builder().id(1L).code("CSE").name("Computer Science").build();
        sampleStudent = Student.builder().id(100L).registerNumber("2026CS101").fullName("Alex Morgan").department(sampleDepartment).year(2).semester(4).isDeleted(false).build();
        sampleSubject = Subject.builder().id(10L).subjectCode("CS401").subjectName("Database Systems").department(sampleDepartment).credits(4).build();
    }

    @Test
    @DisplayName("Generate student attendance PDF report successfully")
    void testGenerateStudentAttendancePdf_Success() {
        StudentAttendanceSummaryResponse summary = StudentAttendanceSummaryResponse.builder()
                .studentId(100L)
                .registerNumber("2026CS101")
                .studentName("Alex Morgan")
                .overallPercentage(85.0)
                .belowThreshold(false)
                .subjectDetails(List.of(
                        SubjectAttendanceDetail.builder()
                                .subjectCode("CS401")
                                .subjectName("Database Systems")
                                .totalClasses(20)
                                .attendedClasses(17)
                                .absentClasses(3)
                                .percentage(85.0)
                                .build()
                ))
                .build();

        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(attendanceService.getStudentAttendanceSummary(100L)).thenReturn(summary);

        byte[] pdfBytes = reportService.generateStudentAttendancePdf(100L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF output byte array must not be empty");
    }

    @Test
    @DisplayName("Generate semester performance PDF report successfully")
    void testGenerateStudentPerformancePdf_Success() {
        SemesterGradeSummaryResponse gradeSummary = SemesterGradeSummaryResponse.builder()
                .studentId(100L)
                .studentName("Alex Morgan")
                .registerNumber("2026CS101")
                .semester(4)
                .academicYear("2025-2026")
                .overallPercentage(88.0)
                .totalSubjects(1)
                .passedSubjects(1)
                .failedSubjects(0)
                .semesterStatus("PASS")
                .grades(List.of(
                        GradeResponse.builder()
                                .subjectCode("CS401")
                                .subjectName("Database Systems")
                                .totalMarks(88.0)
                                .percentage(88.0)
                                .letterGrade("A")
                                .isPassed(true)
                                .build()
                ))
                .build();

        when(gradeService.getStudentSemesterGrades(100L, 4)).thenReturn(gradeSummary);

        byte[] pdfBytes = reportService.generateStudentPerformancePdf(100L, 4);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF output byte array must not be empty");
    }
}
