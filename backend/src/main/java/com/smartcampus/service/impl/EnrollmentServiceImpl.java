package com.smartcampus.service.impl;

import com.smartcampus.dto.request.BatchEnrollmentRequest;
import com.smartcampus.dto.request.EnrollmentRequest;
import com.smartcampus.dto.response.EnrollmentResponse;
import com.smartcampus.entity.Enrollment;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.Subject;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.AuditService;
import com.smartcampus.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentServiceImpl.class);

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AuditService auditService;

    @Autowired
    public EnrollmentServiceImpl(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            AuditService auditService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.auditService = auditService;
    }

    @Override
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        log.info("Enrolling student ID: {} into subject ID: {}", request.getStudentId(), request.getSubjectId());

        boolean exists = enrollmentRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemester(
                request.getStudentId(), request.getSubjectId(), request.getAcademicYear(), request.getSemester());
        if (exists) {
            log.warn("Student {} is already enrolled in subject {} for academic year {} sem {}",
                    request.getStudentId(), request.getSubjectId(), request.getAcademicYear(), request.getSemester());
            throw new DuplicateResourceException("Student is already enrolled in this subject for the specified semester");
        }

        Student student = studentRepository.findByIdAndIsDeletedFalse(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(request.getStudentId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .subject(subject)
                .academicYear(request.getAcademicYear().trim())
                .semester(request.getSemester())
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("Enrollment saved with ID: {}", saved.getId());

        auditService.logAction(null, "ENROLL_STUDENT", "ENROLLMENT", saved.getId(),
                "Enrolled student " + student.getRegisterNumber() + " in subject " + subject.getSubjectCode());

        return mapToResponse(saved);
    }

    @Override
    public List<EnrollmentResponse> batchEnrollStudents(BatchEnrollmentRequest request) {
        log.info("Batch enrolling {} students into subject ID: {}", request.getStudentIds().size(), request.getSubjectId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        List<EnrollmentResponse> responses = new ArrayList<>();

        for (Long studentId : request.getStudentIds()) {
            boolean exists = enrollmentRepository.existsByStudentIdAndSubjectIdAndAcademicYearAndSemester(
                    studentId, request.getSubjectId(), request.getAcademicYear(), request.getSemester());

            if (!exists) {
                studentRepository.findByIdAndIsDeletedFalse(studentId).ifPresent(student -> {
                    Enrollment enrollment = Enrollment.builder()
                            .student(student)
                            .subject(subject)
                            .academicYear(request.getAcademicYear().trim())
                            .semester(request.getSemester())
                            .build();
                    Enrollment saved = enrollmentRepository.save(enrollment);
                    responses.add(mapToResponse(saved));
                });
            }
        }

        log.info("Successfully completed batch enrollment for {} students", responses.size());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrolledStudentsBySubject(
            Long subjectId, String academicYear, Integer semester) {
        if (academicYear != null && semester != null) {
            return enrollmentRepository.findBySubjectIdAndAcademicYearAndSemester(subjectId, academicYear, semester)
                    .stream().map(this::mapToResponse).toList();
        }
        return enrollmentRepository.findBySubjectId(subjectId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void dropEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", enrollmentId));

        enrollmentRepository.delete(enrollment);
        log.info("Dropped enrollment ID: {}", enrollmentId);

        auditService.logAction(null, "DROP_ENROLLMENT", "ENROLLMENT", enrollmentId, "Dropped enrollment " + enrollmentId);
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentRegisterNumber(enrollment.getStudent().getRegisterNumber())
                .studentName(enrollment.getStudent().getFullName())
                .subjectId(enrollment.getSubject().getId())
                .subjectCode(enrollment.getSubject().getSubjectCode())
                .subjectName(enrollment.getSubject().getSubjectName())
                .academicYear(enrollment.getAcademicYear())
                .semester(enrollment.getSemester())
                .enrolledAt(enrollment.getEnrolledAt())
                .build();
    }
}
