package com.smartcampus.service.impl;

import com.smartcampus.dto.response.FacultyDashboardResponse;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.dto.response.StudentDashboardResponse;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Grade;
import com.smartcampus.entity.Student;
import com.smartcampus.enums.AttendanceStatus;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.mapper.StudentMapper;
import com.smartcampus.repository.*;
import com.smartcampus.service.AttendanceService;
import com.smartcampus.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final GradeRepository gradeRepository;
    private final DepartmentRepository departmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;
    private final AttendanceService attendanceService;
    private final StudentMapper studentMapper;

    @Autowired
    public DashboardServiceImpl(
            StudentRepository studentRepository,
            AttendanceRepository attendanceRepository,
            GradeRepository gradeRepository,
            DepartmentRepository departmentRepository,
            EnrollmentRepository enrollmentRepository,
            NotificationRepository notificationRepository,
            AttendanceService attendanceService,
            StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.gradeRepository = gradeRepository;
        this.departmentRepository = departmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.notificationRepository = notificationRepository;
        this.attendanceService = attendanceService;
        this.studentMapper = studentMapper;
    }

    @Override
    public FacultyDashboardResponse getFacultyDashboard() {
        log.info("Generating faculty dashboard analytics");

        long totalStudents = studentRepository.countByIsDeletedFalse();
        LocalDate today = LocalDate.now();
        long presentToday = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.PRESENT);
        long absentToday = attendanceRepository.countByDateAndStatus(today, AttendanceStatus.ABSENT);

        long totalToday = presentToday + absentToday;
        double overallAttendance = totalToday == 0 ? 0.0
                : round((double) presentToday / totalToday * 100.0);

        Double avgPerformance = gradeRepository.findOverallAveragePercentage();
        double performance = avgPerformance != null ? round(avgPerformance) : 0.0;

        Map<String, Long> deptCounts = new HashMap<>();
        List<Department> departments = departmentRepository.findAll();
        for (Department dept : departments) {
            long count = studentRepository.countByDepartmentIdAndIsDeletedFalse(dept.getId());
            deptCounts.put(dept.getCode(), count);
        }

        List<StudentResponse> recentStudents = studentRepository.findAll(
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        ).getContent().stream().map(studentMapper::toResponse).toList();

        return FacultyDashboardResponse.builder()
                .totalStudents(totalStudents)
                .presentToday(presentToday)
                .absentToday(absentToday)
                .overallAttendancePercentage(overallAttendance)
                .averageAcademicPerformance(performance)
                .departmentWiseStudentCount(deptCounts)
                .recentlyAddedStudents(recentStudents)
                .build();
    }

    @Override
    public StudentDashboardResponse getStudentDashboard(Long userId) {
        log.info("Generating student dashboard analytics for user ID: {}", userId);

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new StudentNotFoundException("No student profile linked to user ID: " + userId));

        var attendanceSummary = attendanceService.getStudentAttendanceSummary(student.getId());
        long totalEnrolled = enrollmentRepository.countByStudentId(student.getId());

        Double avgMarks = gradeRepository.findAveragePercentageByStudentId(student.getId());
        double marks = avgMarks != null ? round(avgMarks) : 0.0;

        List<GradeResponse> recentGrades = gradeRepository.findByStudentIdAndIsPublishedTrue(student.getId()).stream()
                .sorted((g1, g2) -> g2.getCreatedAt().compareTo(g1.getCreatedAt()))
                .limit(5)
                .map(this::mapGradeToResponse)
                .toList();

        long unreadNotifs = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return StudentDashboardResponse.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .registerNumber(student.getRegisterNumber())
                .currentSemester(student.getSemester())
                .departmentName(student.getDepartment().getName())
                .overallAttendancePercentage(attendanceSummary.getOverallPercentage())
                .totalEnrolledSubjects(totalEnrolled)
                .averageMarks(marks)
                .recentGrades(recentGrades)
                .unreadNotificationsCount(unreadNotifs)
                .build();
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private GradeResponse mapGradeToResponse(Grade g) {
        return GradeResponse.builder()
                .id(g.getId())
                .studentId(g.getStudent().getId())
                .studentRegisterNumber(g.getStudent().getRegisterNumber())
                .studentName(g.getStudent().getFullName())
                .subjectId(g.getSubject().getId())
                .subjectCode(g.getSubject().getSubjectCode())
                .subjectName(g.getSubject().getSubjectName())
                .semester(g.getSemester())
                .academicYear(g.getAcademicYear())
                .internalMarks(g.getInternalMarks())
                .assignmentMarks(g.getAssignmentMarks())
                .practicalMarks(g.getPracticalMarks())
                .examMarks(g.getExamMarks())
                .totalMarks(g.getTotalMarks())
                .percentage(g.getPercentage())
                .letterGrade(g.getLetterGrade())
                .isPassed(g.getIsPassed())
                .isPublished(g.getIsPublished())
                .createdAt(g.getCreatedAt())
                .build();
    }
}
