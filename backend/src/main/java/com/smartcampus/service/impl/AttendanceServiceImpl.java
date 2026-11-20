package com.smartcampus.service.impl;

import com.smartcampus.dto.request.AttendanceMarkItem;
import com.smartcampus.dto.request.AttendanceUpdateRequest;
import com.smartcampus.dto.request.BatchAttendanceRequest;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.ClassAttendanceSummaryResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse.SubjectAttendanceDetail;
import com.smartcampus.entity.*;
import com.smartcampus.enums.AttendanceStatus;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.repository.*;
import com.smartcampus.service.AttendanceService;
import com.smartcampus.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final FacultyRepository facultyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditService auditService;

    @Value("${app.attendance.low-threshold-percentage:75.0}")
    private double lowAttendanceThreshold;

    @Autowired
    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            FacultyRepository facultyRepository,
            EnrollmentRepository enrollmentRepository,
            AuditService auditService) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.facultyRepository = facultyRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditService = auditService;
    }

    @Override
    public List<AttendanceResponse> markBatchAttendance(Long facultyUserId, BatchAttendanceRequest request) {
        log.info("Marking batch attendance for subject ID: {} on date: {}", 
                request.getSubjectId(), request.getAttendanceDate());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user: " + facultyUserId));

        List<AttendanceResponse> savedResponses = new ArrayList<>();

        for (AttendanceMarkItem item : request.getRecords()) {
            Student student = studentRepository.findByIdAndIsDeletedFalse(item.getStudentId())
                    .orElseThrow(() -> new StudentNotFoundException(item.getStudentId()));

            // Validate that the student is enrolled in the subject
            boolean isEnrolled = enrollmentRepository.existsByStudentIdAndSubjectId(student.getId(), subject.getId());
            if (!isEnrolled) {
                log.warn("Student {} is not enrolled in subject {}. Skipping or rejecting.", student.getId(), subject.getId());
                throw new IllegalArgumentException(
                        "Student " + student.getRegisterNumber() + " is not enrolled in subject " + subject.getSubjectCode());
            }

            // Prevent duplicate attendance for same student, subject and date
            Optional<Attendance> existing = attendanceRepository.findByStudentIdAndSubjectIdAndAttendanceDate(
                    student.getId(), subject.getId(), request.getAttendanceDate());

            Attendance attendance;
            if (existing.isPresent()) {
                // Update existing record with authorization
                attendance = existing.get();
                attendance.setStatus(item.getStatus());
                attendance.setRemarks(item.getRemarks());
                attendance.setMarkedByFaculty(faculty);
            } else {
                attendance = Attendance.builder()
                        .student(student)
                        .subject(subject)
                        .attendanceDate(request.getAttendanceDate())
                        .status(item.getStatus())
                        .remarks(item.getRemarks())
                        .markedByFaculty(faculty)
                        .build();
            }

            Attendance saved = attendanceRepository.save(attendance);
            savedResponses.add(mapToResponse(saved));
        }

        log.info("Batch attendance saved for {} students in subject: {}", savedResponses.size(), subject.getSubjectCode());

        auditService.logAction(facultyUserId, "MARK_ATTENDANCE", "ATTENDANCE", subject.getId(),
                "Marked attendance for " + savedResponses.size() + " students on " + request.getAttendanceDate());

        return savedResponses;
    }

    @Override
    public AttendanceResponse updateAttendance(Long attendanceId, AttendanceUpdateRequest request) {
        log.info("Updating attendance record ID: {}", attendanceId);

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));

        attendance.setStatus(request.getStatus());
        attendance.setRemarks(request.getRemarks());

        Attendance updated = attendanceRepository.save(attendance);
        log.info("Attendance record ID: {} updated to status: {}", attendanceId, request.getStatus());

        auditService.logAction(null, "UPDATE_ATTENDANCE", "ATTENDANCE", attendanceId,
                "Updated attendance ID: " + attendanceId + " to " + request.getStatus());

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceBySubjectAndDate(Long subjectId, LocalDate date) {
        return attendanceRepository.findBySubjectIdAndAttendanceDate(subjectId, date).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentAttendanceSummaryResponse getStudentAttendanceSummary(Long studentId) {
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<SubjectAttendanceDetail> subjectDetails = new ArrayList<>();

        long totalSessionsOverall = 0;
        long totalPresentOverall = 0;
        long totalAbsentOverall = 0;
        long totalOnLeaveOverall = 0;

        for (Enrollment enrollment : enrollments) {
            Subject subject = enrollment.getSubject();
            List<Attendance> attendances = attendanceRepository.findByStudentIdAndSubjectId(studentId, subject.getId());

            long totalClasses = attendances.size();
            long attendedClasses = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
            long absentClasses = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
            long onLeaveClasses = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE).count();

            // Safe percentage calculation: avoids division by zero
            double percentage = totalClasses == 0 ? 0.0 : round((double) attendedClasses / totalClasses * 100.0);
            boolean belowThreshold = totalClasses > 0 && percentage < lowAttendanceThreshold;

            totalSessionsOverall += totalClasses;
            totalPresentOverall += attendedClasses;
            totalAbsentOverall += absentClasses;
            totalOnLeaveOverall += onLeaveClasses;

            subjectDetails.add(SubjectAttendanceDetail.builder()
                    .subjectId(subject.getId())
                    .subjectCode(subject.getSubjectCode())
                    .subjectName(subject.getSubjectName())
                    .totalClasses(totalClasses)
                    .attendedClasses(attendedClasses)
                    .absentClasses(absentClasses)
                    .onLeaveClasses(onLeaveClasses)
                    .percentage(percentage)
                    .belowThreshold(belowThreshold)
                    .build());
        }

        double overallPercentage = totalSessionsOverall == 0 ? 0.0
                : round((double) totalPresentOverall / totalSessionsOverall * 100.0);

        boolean overallBelowThreshold = totalSessionsOverall > 0 && overallPercentage < lowAttendanceThreshold;

        return StudentAttendanceSummaryResponse.builder()
                .studentId(student.getId())
                .registerNumber(student.getRegisterNumber())
                .studentName(student.getFullName())
                .totalSessions(totalSessionsOverall)
                .totalPresent(totalPresentOverall)
                .totalAbsent(totalAbsentOverall)
                .totalOnLeave(totalOnLeaveOverall)
                .overallPercentage(overallPercentage)
                .belowThreshold(overallBelowThreshold)
                .lowThresholdPercentage(lowAttendanceThreshold)
                .subjectDetails(subjectDetails)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassAttendanceSummaryResponse getClassAttendanceSummary(Long subjectId, LocalDate date) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        List<Attendance> records = attendanceRepository.findBySubjectIdAndAttendanceDate(subjectId, date);
        long totalEnrolled = enrollmentRepository.countBySubjectId(subjectId);

        long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ABSENT).count();
        long onLeave = records.stream().filter(r -> r.getStatus() == AttendanceStatus.ON_LEAVE).count();

        double percentage = records.isEmpty() ? 0.0 : round((double) present / records.size() * 100.0);

        return ClassAttendanceSummaryResponse.builder()
                .subjectId(subject.getId())
                .subjectCode(subject.getSubjectCode())
                .subjectName(subject.getSubjectName())
                .attendanceDate(date)
                .totalEnrolled(totalEnrolled)
                .totalPresent(present)
                .totalAbsent(absent)
                .totalOnLeave(onLeave)
                .attendancePercentage(percentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AttendanceResponse> getStudentAttendanceHistory(Long studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Attendance> pageResult = attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(studentId, pageable);

        List<AttendanceResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<AttendanceResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .empty(pageResult.isEmpty())
                .build();
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private AttendanceResponse mapToResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .studentId(a.getStudent().getId())
                .studentRegisterNumber(a.getStudent().getRegisterNumber())
                .studentName(a.getStudent().getFullName())
                .subjectId(a.getSubject().getId())
                .subjectCode(a.getSubject().getSubjectCode())
                .subjectName(a.getSubject().getSubjectName())
                .attendanceDate(a.getAttendanceDate())
                .status(a.getStatus())
                .remarks(a.getRemarks())
                .markedByFacultyId(a.getMarkedByFaculty().getId())
                .markedByFacultyName(a.getMarkedByFaculty().getFullName())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
