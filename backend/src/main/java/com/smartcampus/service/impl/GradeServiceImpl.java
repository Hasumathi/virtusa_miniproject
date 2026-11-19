package com.smartcampus.service.impl;

import com.smartcampus.dto.request.GradeEntryRequest;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.dto.response.SemesterGradeSummaryResponse;
import com.smartcampus.entity.Grade;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.Subject;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.GradeRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.AuditService;
import com.smartcampus.service.GradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeServiceImpl implements GradeService {

    private static final Logger log = LoggerFactory.getLogger(GradeServiceImpl.class);

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditService auditService;

    @Value("${app.grading.pass-threshold:50.0}")
    private double passThreshold;

    @Autowired
    public GradeServiceImpl(
            GradeRepository gradeRepository,
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            EnrollmentRepository enrollmentRepository,
            AuditService auditService) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.auditService = auditService;
    }

    @Override
    public GradeResponse enterOrUpdateGrade(GradeEntryRequest request) {
        log.info("Entering/updating grades for student ID: {}, subject ID: {}, sem: {}",
                request.getStudentId(), request.getSubjectId(), request.getSemester());

        Student student = studentRepository.findByIdAndIsDeletedFalse(request.getStudentId())
                .orElseThrow(() -> new StudentNotFoundException(request.getStudentId()));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.getSubjectId()));

        // Validate enrollment
        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndSubjectId(student.getId(), subject.getId());
        if (!isEnrolled) {
            log.warn("Student {} is not enrolled in subject {}. Cannot assign grade.", student.getId(), subject.getId());
            throw new IllegalArgumentException("Student " + student.getRegisterNumber() + " is not enrolled in subject " + subject.getSubjectCode());
        }

        // Calculate total and percentage
        double internal = request.getInternalMarks() != null ? request.getInternalMarks() : 0.0;
        double assignment = request.getAssignmentMarks() != null ? request.getAssignmentMarks() : 0.0;
        double practical = request.getPracticalMarks() != null ? request.getPracticalMarks() : 0.0;
        double exam = request.getExamMarks() != null ? request.getExamMarks() : 0.0;

        double totalMarks = round(internal + assignment + practical + exam);
        double maxMarks = 100.0;
        double percentage = round((totalMarks / maxMarks) * 100.0);

        String letterGrade = determineLetterGrade(percentage);
        boolean isPassed = percentage >= passThreshold;

        Optional<Grade> existing = gradeRepository.findByStudentIdAndSubjectIdAndSemesterAndAcademicYear(
                student.getId(), subject.getId(), request.getSemester(), request.getAcademicYear());

        Grade grade;
        if (existing.isPresent()) {
            grade = existing.get();
            grade.setInternalMarks(internal);
            grade.setAssignmentMarks(assignment);
            grade.setPracticalMarks(practical);
            grade.setExamMarks(exam);
            grade.setTotalMarks(totalMarks);
            grade.setPercentage(percentage);
            grade.setLetterGrade(letterGrade);
            grade.setIsPassed(isPassed);
        } else {
            grade = Grade.builder()
                    .student(student)
                    .subject(subject)
                    .semester(request.getSemester())
                    .academicYear(request.getAcademicYear().trim())
                    .internalMarks(internal)
                    .assignmentMarks(assignment)
                    .practicalMarks(practical)
                    .examMarks(exam)
                    .totalMarks(totalMarks)
                    .percentage(percentage)
                    .letterGrade(letterGrade)
                    .isPassed(isPassed)
                    .isPublished(false) // Unpublished until faculty triggers publication
                    .build();
        }

        Grade saved = gradeRepository.save(grade);
        log.info("Grade saved: ID={}, Total={}, Grade={}, Passed={}", saved.getId(), totalMarks, letterGrade, isPassed);

        auditService.logAction(null, "ENTER_GRADE", "GRADE", saved.getId(),
                "Entered grade for student " + student.getRegisterNumber() + " in subject " + subject.getSubjectCode());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGradeById(Long gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade", "id", gradeId));
        return mapToResponse(grade);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getGradesBySubjectAndSemester(Long subjectId, Integer semester) {
        return gradeRepository.findBySubjectIdAndSemester(subjectId, semester).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentIdAndIsPublishedTrue(studentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SemesterGradeSummaryResponse getStudentSemesterGrades(Long studentId, Integer semester) {
        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        List<Grade> grades = gradeRepository.findByStudentIdAndSemesterAndIsPublishedTrue(studentId, semester);
        List<GradeResponse> gradeResponses = grades.stream().map(this::mapToResponse).toList();

        int totalSubjects = grades.size();
        int passedSubjects = (int) grades.stream().filter(Grade::getIsPassed).count();
        int failedSubjects = totalSubjects - passedSubjects;

        double overallPercentage = grades.isEmpty() ? 0.0
                : round(grades.stream().mapToDouble(Grade::getPercentage).average().orElse(0.0));

        String semesterStatus = (totalSubjects > 0 && failedSubjects == 0) ? "PASS" : "FAIL";

        String academicYear = grades.isEmpty() ? "N/A" : grades.getFirst().getAcademicYear();

        return SemesterGradeSummaryResponse.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .registerNumber(student.getRegisterNumber())
                .semester(semester)
                .academicYear(academicYear)
                .grades(gradeResponses)
                .overallPercentage(overallPercentage)
                .totalSubjects(totalSubjects)
                .passedSubjects(passedSubjects)
                .failedSubjects(failedSubjects)
                .semesterStatus(semesterStatus)
                .build();
    }

    @Override
    public void publishGrades(Long subjectId, Integer semester, String academicYear) {
        log.info("Publishing results for subject ID: {}, semester: {}, academic year: {}",
                subjectId, semester, academicYear);

        List<Grade> grades = gradeRepository.findBySubjectIdAndSemester(subjectId, semester).stream()
                .filter(g -> g.getAcademicYear().equalsIgnoreCase(academicYear.trim()))
                .toList();

        for (Grade g : grades) {
            g.setIsPublished(true);
        }
        gradeRepository.saveAll(grades);

        log.info("Successfully published {} grade records for subject ID: {}", grades.size(), subjectId);

        auditService.logAction(null, "PUBLISH_GRADES", "GRADE", subjectId,
                "Published grades for subject " + subjectId + " semester " + semester);
    }

    private String determineLetterGrade(double percentage) {
        if (percentage >= 90.0) return "A+";
        if (percentage >= 80.0) return "A";
        if (percentage >= 70.0) return "B";
        if (percentage >= 60.0) return "C";
        if (percentage >= 50.0) return "D";
        return "F";
    }

    private double round(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private GradeResponse mapToResponse(Grade g) {
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
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}
