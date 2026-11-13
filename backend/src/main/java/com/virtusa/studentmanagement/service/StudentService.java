package com.virtusa.studentmanagement.service;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.exception.DuplicateEmailException;
import com.virtusa.studentmanagement.exception.StudentNotFoundException;
import com.virtusa.studentmanagement.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
