package com.smartcampus.service;

import com.smartcampus.dto.request.GradeEntryRequest;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.entity.*;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.GradeRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.impl.GradeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private GradeRepository gradeRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Student sampleStudent;
    private Subject sampleSubject;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gradeService, "passThreshold", 50.0);

        Department dept = Department.builder().id(1L).code("CSE").name("Computer Science").build();
        sampleStudent = Student.builder().id(100L).registerNumber("2026CS101").fullName("Alex Morgan").isDeleted(false).build();
        sampleSubject = Subject.builder().id(10L).subjectCode("CS401").subjectName("Database Systems").department(dept).build();
    }

    @Test
    @DisplayName("Calculate total, percentage, letter grade A+, and pass status correctly")
    void testGradeCalculation_GradeAPlus() {
        GradeEntryRequest request = GradeEntryRequest.builder()
                .studentId(100L)
                .subjectId(10L)
                .semester(4)
                .academicYear("2025-2026")
                .internalMarks(23.0)
                .assignmentMarks(14.0)
                .practicalMarks(18.0)
                .examMarks(38.0)
                .build(); // Total = 93.0 -> 93% -> A+ -> Passed

        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(sampleSubject));
        when(enrollmentRepository.existsByStudentIdAndSubjectId(100L, 10L)).thenReturn(true);
        when(gradeRepository.findByStudentIdAndSubjectIdAndSemesterAndAcademicYear(100L, 10L, 4, "2025-2026"))
                .thenReturn(Optional.empty());
        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> {
            Grade g = i.getArgument(0);
            g.setId(1L);
            return g;
        });

        GradeResponse response = gradeService.enterOrUpdateGrade(request);

        assertNotNull(response);
        assertEquals(93.0, response.getTotalMarks());
        assertEquals(93.0, response.getPercentage());
        assertEquals("A+", response.getLetterGrade());
        assertTrue(response.getIsPassed());
        assertTrue(response.getIsPublished());
    }

    @Test
    @DisplayName("Calculate failing grade correctly when below 50%")
    void testGradeCalculation_GradeFail() {
        GradeEntryRequest request = GradeEntryRequest.builder()
                .studentId(100L)
                .subjectId(10L)
                .semester(4)
                .academicYear("2025-2026")
                .internalMarks(10.0)
                .assignmentMarks(5.0)
                .practicalMarks(8.0)
                .examMarks(15.0)
                .build(); // Total = 38.0 -> 38% -> F -> Failed

        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(sampleSubject));
        when(enrollmentRepository.existsByStudentIdAndSubjectId(100L, 10L)).thenReturn(true);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> {
            Grade g = i.getArgument(0);
            g.setId(2L);
            return g;
        });

        GradeResponse response = gradeService.enterOrUpdateGrade(request);

        assertNotNull(response);
        assertEquals(38.0, response.getTotalMarks());
        assertEquals("F", response.getLetterGrade());
        assertFalse(response.getIsPassed());
    }

    @Test
    @DisplayName("Auto-enroll student in subject if not enrolled during grade entry")
    void testGradeCalculation_StudentAutoEnrolled() {
        GradeEntryRequest request = GradeEntryRequest.builder()
                .studentId(100L)
                .subjectId(10L)
                .semester(4)
                .academicYear("2025-2026")
                .examMarks(30.0)
                .build();

        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(subjectRepository.findById(10L)).thenReturn(Optional.of(sampleSubject));
        when(enrollmentRepository.existsByStudentIdAndSubjectId(100L, 10L)).thenReturn(false);
        when(gradeRepository.save(any(Grade.class))).thenAnswer(i -> {
            Grade g = i.getArgument(0);
            g.setId(3L);
            return g;
        });

        GradeResponse response = gradeService.enterOrUpdateGrade(request);

        assertNotNull(response);
        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(gradeRepository).save(any(Grade.class));
    }
}
