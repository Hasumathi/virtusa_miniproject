package com.smartcampus.service;

import com.smartcampus.dto.request.StudentCreateRequest;
import com.smartcampus.dto.request.StudentUpdateRequest;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.enums.AccountStatus;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.mapper.StudentMapper;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Spy
    private StudentMapper studentMapper = new StudentMapper();

    @InjectMocks
    private StudentServiceImpl studentService;

    private Department sampleDepartment;
    private Student sampleStudent;
    private StudentCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleDepartment = Department.builder()
                .id(1L)
                .code("CSE")
                .name("Computer Science")
                .build();

        sampleStudent = Student.builder()
                .id(100L)
                .registerNumber("2026CS101")
                .fullName("Alex Morgan")
                .email("alex.m@campus.edu")
                .phone("9876543210")
                .department(sampleDepartment)
                .year(2)
                .semester(4)
                .section("A")
                .dateOfJoining(LocalDate.of(2024, 8, 1))
                .accountStatus(AccountStatus.ACTIVE)
                .isDeleted(false)
                .build();

        createRequest = StudentCreateRequest.builder()
                .registerNumber("2026CS101")
                .fullName("Alex Morgan")
                .email("alex.m@campus.edu")
                .phone("9876543210")
                .departmentId(1L)
                .year(2)
                .semester(4)
                .section("A")
                .dateOfJoining(LocalDate.of(2024, 8, 1))
                .createAccount(true)
                .build();
    }

    @Test
    @DisplayName("Create student successfully")
    void testCreateStudent_Success() {
        when(studentRepository.existsByRegisterNumber(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentResponse response = studentService.createStudent(createRequest);

        assertNotNull(response);
        assertEquals("2026CS101", response.getRegisterNumber());
        assertEquals("Alex Morgan", response.getFullName());
        assertEquals("CSE", response.getDepartmentCode());
        verify(studentRepository, times(1)).save(any(Student.class));
        verify(auditService, times(1)).logAction(any(), eq("CREATE_STUDENT"), eq("STUDENT"), eq(100L), anyString());
    }

    @Test
    @DisplayName("Create student throws DuplicateResourceException for existing register number")
    void testCreateStudent_DuplicateRegisterNumber() {
        when(studentRepository.existsByRegisterNumber("2026CS101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(createRequest));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Create student throws ResourceNotFoundException when department is invalid")
    void testCreateStudent_DepartmentNotFound() {
        when(studentRepository.existsByRegisterNumber(anyString())).thenReturn(false);
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.createStudent(createRequest));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Get student by ID returns response when found and active")
    void testGetStudentById_Success() {
        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));

        StudentResponse response = studentService.getStudentById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("2026CS101", response.getRegisterNumber());
    }

    @Test
    @DisplayName("Get student by ID throws StudentNotFoundException when not found")
    void testGetStudentById_NotFound() {
        when(studentRepository.findByIdAndIsDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getStudentById(999L));
    }

    @Test
    @DisplayName("Update student successfully modifies fields")
    void testUpdateStudent_Success() {
        StudentUpdateRequest updateRequest = StudentUpdateRequest.builder()
                .fullName("Alex Morgan Updated")
                .phone("9998887777")
                .departmentId(1L)
                .year(3)
                .semester(5)
                .section("B")
                .build();

        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

        StudentResponse response = studentService.updateStudent(100L, updateRequest);

        assertNotNull(response);
        verify(studentRepository, times(1)).save(sampleStudent);
        verify(auditService, times(1)).logAction(any(), eq("UPDATE_STUDENT"), eq("STUDENT"), eq(100L), anyString());
    }

    @Test
    @DisplayName("Delete student marks isDeleted=true (soft delete)")
    void testDeleteStudent_SoftDelete() {
        when(studentRepository.findByIdAndIsDeletedFalse(100L)).thenReturn(Optional.of(sampleStudent));

        studentService.deleteStudent(100L);

        assertTrue(sampleStudent.getIsDeleted());
        assertEquals(AccountStatus.INACTIVE, sampleStudent.getAccountStatus());
        verify(studentRepository, times(1)).save(sampleStudent);
        verify(auditService, times(1)).logAction(any(), eq("DEACTIVATE_STUDENT"), eq("STUDENT"), eq(100L), anyString());
    }
}
