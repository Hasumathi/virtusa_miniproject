package com.smartcampus.service.impl;

import com.smartcampus.dto.request.StudentCreateRequest;
import com.smartcampus.dto.request.StudentUpdateRequest;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Enrollment;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.Subject;
import com.smartcampus.entity.User;
import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.Role;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.mapper.StudentMapper;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.EnrollmentRepository;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.service.AuditService;
import com.smartcampus.service.StudentService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SubjectRepository subjectRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public StudentServiceImpl(
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            StudentMapper studentMapper,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            @Autowired(required = false) SubjectRepository subjectRepository,
            @Autowired(required = false) EnrollmentRepository enrollmentRepository) {
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.subjectRepository = subjectRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public StudentResponse createStudent(StudentCreateRequest request) {
        log.info("Initiating student creation for register number: {}", request.getRegisterNumber());

        String regNo = request.getRegisterNumber().trim().toUpperCase();
        if (studentRepository.existsByRegisterNumber(regNo)) {
            Optional<Student> existingOpt = studentRepository.findByRegisterNumber(regNo);
            if (existingOpt.isPresent() && Boolean.TRUE.equals(existingOpt.get().getIsDeleted())) {
                Student legacy = existingOpt.get();
                legacy.setRegisterNumber(legacy.getRegisterNumber() + "_DEL_" + legacy.getId());
                studentRepository.save(legacy);
            } else {
                log.warn("Attempted duplicate registration number: {}", regNo);
                throw new DuplicateResourceException("Student", "registerNumber", regNo);
            }
        }

        String email = request.getEmail().trim().toLowerCase();
        if (studentRepository.existsByEmail(email) || userRepository.existsByEmail(email)) {
            Optional<Student> existingEmailOpt = studentRepository.findByEmail(email);
            if (existingEmailOpt.isPresent() && Boolean.TRUE.equals(existingEmailOpt.get().getIsDeleted())) {
                Student legacy = existingEmailOpt.get();
                legacy.setEmail("del_" + legacy.getId() + "_" + legacy.getEmail());
                studentRepository.save(legacy);
            } else {
                log.warn("Attempted duplicate email address: {}", email);
                throw new DuplicateResourceException("User/Student", "email", email);
            }
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Student student = studentMapper.toEntity(request, department);
        student.setAccountStatus(AccountStatus.ACTIVE);

        if (Boolean.TRUE.equals(request.getCreateAccount())) {
            String rawPassword = StringUtils.hasText(request.getInitialPassword())
                    ? request.getInitialPassword()
                    : regNo + "@Smart2026";

            student.setInitialPassword(rawPassword);

            User user = User.builder()
                    .username(regNo)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(Role.STUDENT)
                    .accountStatus(AccountStatus.ACTIVE)
                    .build();

            User savedUser = userRepository.save(user);
            student.setUser(savedUser);
            log.info("Created linked User account for student: username={}", regNo);
        }

        Student savedStudent = studentRepository.save(student);
        log.info("Student successfully created with ID: {}, RegisterNumber: {}", savedStudent.getId(), regNo);

        // Auto-enroll student into subjects for their department and semester
        if (subjectRepository != null && enrollmentRepository != null) {
            try {
                List<Subject> subjects = subjectRepository.findByDepartmentIdAndSemester(
                        department.getId(), savedStudent.getSemester());
                if (subjects == null || subjects.isEmpty()) {
                    subjects = subjectRepository.findByDepartmentId(department.getId());
                }

                String academicYear = "2026-2027";

                if (subjects != null) {
                    for (Subject sub : subjects) {
                        boolean alreadyEnrolled = enrollmentRepository.existsByStudentIdAndSubjectId(
                                savedStudent.getId(), sub.getId());
                        if (!alreadyEnrolled) {
                            Enrollment enrollment = Enrollment.builder()
                                    .student(savedStudent)
                                    .subject(sub)
                                    .academicYear(academicYear)
                                    .semester(sub.getSemester() != null ? sub.getSemester() : savedStudent.getSemester())
                                    .build();
                            enrollmentRepository.save(enrollment);
                            log.info("Auto-enrolled student {} in subject {}", regNo, sub.getSubjectCode());
                        }
                    }
                }
            } catch (Exception ex) {
                log.warn("Non-critical error auto-enrolling student {}: {}", regNo, ex.getMessage());
            }
        }

        auditService.logAction(
                null,
                "CREATE_STUDENT",
                "STUDENT",
                savedStudent.getId(),
                "Created student record for: " + regNo + " (" + savedStudent.getFullName() + ")"
        );

        return studentMapper.toResponse(savedStudent);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        log.debug("Fetching student with ID: {}", id);
        Student student = studentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Student not found with ID: {}", id);
                    return new StudentNotFoundException(id);
                });
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentByRegisterNumber(String registerNumber) {
        String regNo = registerNumber.trim().toUpperCase();
        log.debug("Fetching student with Register Number: {}", regNo);
        Student student = studentRepository.findByRegisterNumberAndIsDeletedFalse(regNo)
                .orElseThrow(() -> {
                    log.warn("Student not found with Register Number: {}", regNo);
                    return new StudentNotFoundException(regNo);
                });
        return studentMapper.toResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StudentResponse> getAllStudents(
            String search,
            Long departmentId,
            Integer year,
            Integer semester,
            String section,
            AccountStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Student> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted students
            predicates.add(cb.isFalse(root.get("isDeleted")));

            // Search by name or register number
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), searchPattern);
                Predicate regNoMatch = cb.like(cb.lower(root.get("registerNumber")), searchPattern);
                predicates.add(cb.or(nameMatch, regNoMatch));
            }

            // Filter by department
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            }

            // Filter by year
            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            // Filter by semester
            if (semester != null) {
                predicates.add(cb.equal(root.get("semester"), semester));
            }

            // Filter by section
            if (StringUtils.hasText(section)) {
                predicates.add(cb.equal(cb.upper(root.get("section")), section.trim().toUpperCase()));
            }

            // Filter by account status
            if (status != null) {
                predicates.add(cb.equal(root.get("accountStatus"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Student> studentPage = studentRepository.findAll(spec, pageable);
        List<StudentResponse> content = studentPage.getContent().stream()
                .map(studentMapper::toResponse)
                .toList();

        return PagedResponse.<StudentResponse>builder()
                .content(content)
                .pageNumber(studentPage.getNumber())
                .pageSize(studentPage.getSize())
                .totalElements(studentPage.getTotalElements())
                .totalPages(studentPage.getTotalPages())
                .first(studentPage.isFirst())
                .last(studentPage.isLast())
                .empty(studentPage.isEmpty())
                .build();
    }

    @Override
    public StudentResponse updateStudent(Long id, StudentUpdateRequest request) {
        log.info("Updating student record with ID: {}", id);
        Student student = studentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Cannot update. Student not found with ID: {}", id);
                    return new StudentNotFoundException(id);
                });

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        studentMapper.updateEntityFromRequest(student, request, department);

        if (StringUtils.hasText(request.getRegisterNumber())) {
            String newRegNo = request.getRegisterNumber().trim().toUpperCase();
            if (!newRegNo.equalsIgnoreCase(student.getRegisterNumber())) {
                if (studentRepository.existsByRegisterNumberAndIdNot(newRegNo, id)) {
                    log.warn("Attempted duplicate registration number for update: {}", newRegNo);
                    throw new DuplicateResourceException("Student", "registerNumber", newRegNo);
                }
                student.setRegisterNumber(newRegNo);
                if (student.getUser() != null) {
                    student.getUser().setUsername(newRegNo);
                    userRepository.save(student.getUser());
                }
            }
        }

        if (StringUtils.hasText(request.getEmail())) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(student.getEmail())) {
                if (studentRepository.existsByEmailAndIdNot(newEmail, id) || userRepository.existsByEmail(newEmail)) {
                    log.warn("Attempted duplicate email address for update: {}", newEmail);
                    throw new DuplicateResourceException("User/Student", "email", newEmail);
                }
                student.setEmail(newEmail);
                if (student.getUser() != null) {
                    student.getUser().setEmail(newEmail);
                    userRepository.save(student.getUser());
                }
            }
        }

        if (request.getAccountStatus() != null && student.getUser() != null) {
            student.getUser().setAccountStatus(request.getAccountStatus());
            userRepository.save(student.getUser());
        }

        if (StringUtils.hasText(request.getInitialPassword())) {
            String newPassword = request.getInitialPassword().trim();
            student.setInitialPassword(newPassword);
            if (student.getUser() != null) {
                student.getUser().setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(student.getUser());
            }
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Successfully updated student record: ID={}", updatedStudent.getId());

        auditService.logAction(
                null,
                "UPDATE_STUDENT",
                "STUDENT",
                updatedStudent.getId(),
                "Updated student information for ID: " + id
        );

        return studentMapper.toResponse(updatedStudent);
    }

    @Override
    public void deleteStudent(Long id) {
        log.info("Soft-deleting student record with ID: {}", id);
        Student student = studentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> {
                    log.warn("Cannot delete. Student not found with ID: {}", id);
                    return new StudentNotFoundException(id);
                });

        // Soft deletion preserves academic history while freeing the unique registerNumber and email for re-use
        student.setIsDeleted(true);
        student.setAccountStatus(AccountStatus.INACTIVE);
        student.setRegisterNumber(student.getRegisterNumber() + "_DEL_" + id);
        student.setEmail("del_" + id + "_" + student.getEmail());

        if (student.getUser() != null) {
            student.getUser().setAccountStatus(AccountStatus.INACTIVE);
            student.getUser().setUsername(student.getUser().getUsername() + "_DEL_" + id);
            student.getUser().setEmail("del_" + id + "_" + student.getUser().getEmail());
            userRepository.save(student.getUser());
        }

        studentRepository.save(student);
        log.info("Student successfully deactivated and soft-deleted (registerNumber freed): ID={}", id);

        auditService.logAction(
                null,
                "DEACTIVATE_STUDENT",
                "STUDENT",
                id,
                "Soft-deleted and deactivated student record for ID: " + id
        );
    }

    @Override
    public StudentResponse updateStudentStatus(Long id, AccountStatus status) {
        log.info("Updating account status for student ID: {} to {}", id, status);
        Student student = studentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        student.setAccountStatus(status);
        if (student.getUser() != null) {
            student.getUser().setAccountStatus(status);
            userRepository.save(student.getUser());
        }

        Student updatedStudent = studentRepository.save(student);
        log.info("Updated student status: ID={}, Status={}", id, status);

        auditService.logAction(
                null,
                "UPDATE_STUDENT_STATUS",
                "STUDENT",
                id,
                "Changed account status to " + status + " for student ID: " + id
        );

        return studentMapper.toResponse(updatedStudent);
    }
}
