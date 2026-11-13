package com.virtusa.studentmanagement.service;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.exception.DuplicateEmailException;
import com.virtusa.studentmanagement.exception.InvalidStudentDataException;
import com.virtusa.studentmanagement.exception.StudentNotFoundException;
import com.virtusa.studentmanagement.repository.StudentRepository;
import com.virtusa.studentmanagement.util.CsvHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Service Layer for Student Management.
 * 
 * - Enforces business rules and data consistency.
 * - Throws domain-specific custom exceptions (StudentNotFoundException, DuplicateEmailException).
 * - Comprehensive SLF4J logging for all CRUD and query actions.
 */
@Service
@Transactional
public class StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Retrieve all student records.
     */
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        logger.info("Fetching all students from database");
        List<Student> students = studentRepository.findAll();
        logger.debug("Retrieved {} student record(s)", students.size());
        return students;
    }

    /**
     * Retrieve students with pagination and dynamic sorting.
     */
    @Transactional(readOnly = true)
    public Page<Student> getStudentsPaginated(int page, int size, String sortBy, String direction) {
        logger.info("Fetching students page {} (size: {}, sortBy: {}, direction: {})", page, size, sortBy, direction);
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return studentRepository.findAll(pageable);
    }

    /**
     * Search students with pagination and sorting.
     */
    @Transactional(readOnly = true)
    public Page<Student> searchStudentsPaginated(String keyword, int page, int size, String sortBy, String direction) {
        logger.info("Searching students with pagination keyword: '{}', page: {}, size: {}", keyword, page, size);
        Sort sort = direction.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return studentRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * Retrieve student by unique ID.
     * Throws StudentNotFoundException if record does not exist.
     */
    @Transactional(readOnly = true)
    public Student getStudentById(Long id) {
        logger.info("Fetching student with ID: {}", id);
        return studentRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Student not found with ID: {}", id);
                    return new StudentNotFoundException(id);
                });
    }

    /**
     * Create a new student record.
     * Throws DuplicateEmailException if email is already registered.
     */
    public Student createStudent(Student student) {
        logger.info("Attempting to create student with email: {}", student.getEmail());

        if (studentRepository.existsByEmail(student.getEmail())) {
            logger.warn("Student creation rejected: Duplicate email '{}'", student.getEmail());
            throw new DuplicateEmailException(student.getEmail(), "create");
        }

        student.setId(null);
        Student savedStudent = studentRepository.save(student);
        logger.info("Successfully created student with ID: {} and email: {}", savedStudent.getId(), savedStudent.getEmail());
        return savedStudent;
    }

    /**
     * Update an existing student record.
     * Throws StudentNotFoundException if student does not exist.
     * Throws DuplicateEmailException if email is taken by another student.
     */
    public Student updateStudent(Long id, Student updatedStudent) {
        logger.info("Attempting to update student with ID: {}", id);
        Student existingStudent = getStudentById(id);

        if (studentRepository.existsByEmailAndIdNot(updatedStudent.getEmail(), id)) {
            logger.warn("Student update rejected for ID {}: Duplicate email '{}'", id, updatedStudent.getEmail());
            throw new DuplicateEmailException(updatedStudent.getEmail(), "update");
        }

        existingStudent.setName(updatedStudent.getName());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setDepartment(updatedStudent.getDepartment());
        existingStudent.setYearOfStudy(updatedStudent.getYearOfStudy());
        existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());

        Student savedStudent = studentRepository.save(existingStudent);
        logger.info("Successfully updated student with ID: {}", savedStudent.getId());
        return savedStudent;
    }

    /**
     * Delete student by ID.
     * Throws StudentNotFoundException if student does not exist.
     */
    public void deleteStudent(Long id) {
        logger.info("Attempting to delete student with ID: {}", id);
        Student student = getStudentById(id);
        studentRepository.delete(student);
        logger.info("Successfully deleted student with ID: {}", id);
    }

    /**
     * Search students by keyword (name or department).
     */
    @Transactional(readOnly = true)
    public List<Student> searchStudents(String keyword) {
        logger.info("Searching students with keyword: '{}'", keyword);
        List<Student> results = studentRepository.searchByKeyword(keyword);
        logger.debug("Found {} match(es) for keyword: '{}'", results.size(), keyword);
        return results;
    }

    /**
     * Filter students by department name.
     */
    @Transactional(readOnly = true)
    public List<Student> getStudentsByDepartment(String department) {
        logger.info("Filtering students by department: '{}'", department);
        return studentRepository.findByDepartmentIgnoreCase(department);
    }

    /**
     * Filter students by year of study.
     */
    @Transactional(readOnly = true)
    public List<Student> getStudentsByYear(Integer year) {
        logger.info("Filtering students by academic year: {}", year);
        return studentRepository.findByYearOfStudy(year);
    }

    /**
     * Export all students to a CSV byte stream.
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportStudentsCsv() {
        logger.info("Exporting all students to CSV stream");
        List<Student> students = studentRepository.findAll();
        return CsvHelper.studentsToCsv(students);
    }

    /**
     * Bulk import students from an uploaded CSV file.
     * Validates records and ensures email uniqueness.
     */
    public int importStudentsCsv(MultipartFile file) {
        logger.info("Importing students from CSV file: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            throw new InvalidStudentDataException("Uploaded CSV file is empty");
        }

        try {
            List<Student> students = CsvHelper.csvToStudents(file.getInputStream());
            for (Student s : students) {
                if (studentRepository.existsByEmail(s.getEmail())) {
                    throw new DuplicateEmailException(s.getEmail(), "import");
                }
            }
            List<Student> saved = studentRepository.saveAll(students);
            logger.info("Successfully imported {} students from CSV", saved.size());
            return saved.size();
        } catch (IOException e) {
            logger.error("Failed to read CSV input stream: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }
    }
}
