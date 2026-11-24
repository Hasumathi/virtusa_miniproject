package com.smartcampus.service;

import com.smartcampus.dto.response.FacultyDashboardResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.dto.response.StudentDashboardResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Student;
import com.smartcampus.enums.AttendanceStatus;
import com.smartcampus.mapper.StudentMapper;
import com.smartcampus.repository.*;
import com.smartcampus.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private AttendanceService attendanceService;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Student sampleStudent;
    private Department sampleDept;

    @BeforeEach
    void setUp() {
        sampleDept = Department.builder().id(1L).code("CSE").name("Computer Science").build();
        sampleStudent = Student.builder().id(100L).registerNumber("2026CS101").fullName("Alex Morgan").department(sampleDept).year(2).semester(4).isDeleted(false).build();
    }

    @Test
    @DisplayName("Generate Faculty Dashboard metrics correctly")
    void testGetFacultyDashboard_Success() {
        when(studentRepository.countByIsDeletedFalse()).thenReturn(150L);
        when(attendanceRepository.countByDateAndStatus(any(LocalDate.class), eq(AttendanceStatus.PRESENT))).thenReturn(130L);
        when(attendanceRepository.countByDateAndStatus(any(LocalDate.class), eq(AttendanceStatus.ABSENT))).thenReturn(20L);
        when(gradeRepository.findOverallAveragePercentage()).thenReturn(78.5);
        when(departmentRepository.findAll()).thenReturn(List.of(sampleDept));
        when(studentRepository.countByDepartmentIdAndIsDeletedFalse(1L)).thenReturn(80L);
        when(studentRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        FacultyDashboardResponse response = dashboardService.getFacultyDashboard();

        assertNotNull(response);
        assertEquals(150L, response.getTotalStudents());
        assertEquals(130L, response.getPresentToday());
        assertEquals(20L, response.getAbsentToday());
        assertEquals(86.67, response.getOverallAttendancePercentage());
        assertEquals(78.5, response.getAverageAcademicPerformance());
        assertEquals(80L, response.getDepartmentWiseStudentCount().get("CSE"));
    }

    @Test
    @DisplayName("Generate Student Dashboard metrics correctly")
    void testGetStudentDashboard_Success() {
        StudentAttendanceSummaryResponse attendanceSummary = StudentAttendanceSummaryResponse.builder()
                .overallPercentage(82.5)
                .build();

        when(studentRepository.findByUserId(1L)).thenReturn(Optional.of(sampleStudent));
        when(attendanceService.getStudentAttendanceSummary(100L)).thenReturn(attendanceSummary);
        when(enrollmentRepository.countByStudentId(100L)).thenReturn(5L);
        when(gradeRepository.findAveragePercentageByStudentId(100L)).thenReturn(85.0);
        when(gradeRepository.findByStudentIdAndIsPublishedTrue(100L)).thenReturn(Collections.emptyList());
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

        StudentDashboardResponse response = dashboardService.getStudentDashboard(1L);

        assertNotNull(response);
        assertEquals(100L, response.getStudentId());
        assertEquals("Alex Morgan", response.getStudentName());
        assertEquals(82.5, response.getOverallAttendancePercentage());
        assertEquals(5L, response.getTotalEnrolledSubjects());
        assertEquals(85.0, response.getAverageMarks());
        assertEquals(3L, response.getUnreadNotificationsCount());
    }
}
