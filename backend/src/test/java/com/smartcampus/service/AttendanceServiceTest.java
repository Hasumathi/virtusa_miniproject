package com.smartcampus.service;

import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.entity.*;
import com.smartcampus.enums.AttendanceStatus;
import com.smartcampus.repository.*;
import com.smartcampus.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Student sampleStudent;
    private Subject sampleSubject;
    private Enrollment sampleEnrollment;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceService, "lowAttendanceThreshold", 75.0);

        Department dept = Department.builder().id(1L).code("CSE").name("Computer Science").build();

        sampleStudent = Student.builder()
                .id(100L)
                .registerNumber("2026CS101")
                .fullName("Alex Morgan")
                .department(dept)
                .year(2)
                .semester(4)
                .isDeleted(false)
                .build();

        sampleSubject = Subject.builder()
                .id(10L)
                .subjectCode("CS401")
                .subjectName("Database Systems")
                .department(dept)
                .semester(4)
                .credits(4)
                .build();

        sampleEnrollment = Enrollment.builder()
                .id(50L)
                .student(sampleStudent)
                .subject(sampleSubject)
                .academicYear("2025-2026")
                .semester(4)
                .build();
    }

    @Test
    @DisplayName("Calculate attendance summary accurately (80% attendance)")
    void testAttendanceCalculation_AccuratePercentage() {
        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(enrollmentRepository.findByStudentId(100L)).thenReturn(List.of(sampleEnrollment));

        List<Attendance> attendances = List.of(
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.ABSENT).build()
        ); // 4 out of 5 = 80.0%

        when(attendanceRepository.findByStudentIdAndSubjectId(100L, 10L)).thenReturn(attendances);

        StudentAttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(100L);

        assertNotNull(summary);
        assertEquals(5, summary.getTotalSessions());
        assertEquals(4, summary.getTotalPresent());
        assertEquals(1, summary.getTotalAbsent());
        assertEquals(80.0, summary.getOverallPercentage());
        assertFalse(summary.isBelowThreshold());
    }

    @Test
    @DisplayName("Detect low attendance warning below 75% threshold")
    void testAttendanceCalculation_BelowThreshold() {
        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(enrollmentRepository.findByStudentId(100L)).thenReturn(List.of(sampleEnrollment));

        List<Attendance> attendances = List.of(
                Attendance.builder().status(AttendanceStatus.PRESENT).build(),
                Attendance.builder().status(AttendanceStatus.ABSENT).build(),
                Attendance.builder().status(AttendanceStatus.ABSENT).build()
        ); // 1 out of 3 = 33.33%

        when(attendanceRepository.findByStudentIdAndSubjectId(100L, 10L)).thenReturn(attendances);

        StudentAttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(100L);

        assertNotNull(summary);
        assertEquals(3, summary.getTotalSessions());
        assertEquals(1, summary.getTotalPresent());
        assertEquals(33.33, summary.getOverallPercentage());
        assertTrue(summary.isBelowThreshold());
    }

    @Test
    @DisplayName("Handle zero attendance sessions safely without division by zero")
    void testAttendanceCalculation_ZeroSessionsSafe() {
        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(enrollmentRepository.findByStudentId(100L)).thenReturn(List.of(sampleEnrollment));
        when(attendanceRepository.findByStudentIdAndSubjectId(100L, 10L)).thenReturn(Collections.emptyList());

        StudentAttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(100L);

        assertNotNull(summary);
        assertEquals(0, summary.getTotalSessions());
        assertEquals(0.0, summary.getOverallPercentage());
        assertFalse(summary.isBelowThreshold());
    }
}
