package com.virtusa.studentmanagement.repository;

import com.virtusa.studentmanagement.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for the Student Entity.
 * 
 * Extends JpaRepository to inherit all standard CRUD operations:
 * - save(Student student): Create or Update
 * - findById(Long id): Retrieve by ID
 * - findAll(): Retrieve all students
 * - deleteById(Long id): Delete by ID
 * - count(): Count total records
 * - existsById(Long id): Check existence by ID
 * 
 * Custom query methods provide direct support for:
 * - Duplicate email checks
 * - Search and filtering by name/department
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find a student by their unique email address.
     * 
     * @param email student email
     * @return Optional containing the student if found, empty otherwise
     */
    Optional<Student> findByEmail(String email);

    /**
     * Check if a student with the given email already exists in the database.
     * Used to prevent duplicate email entries during student creation.
     * 
     * @param email student email
     * @return true if email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if an email is already taken by another student during an update operation.
     * 
     * @param email student email
     * @param id current student ID being edited
     * @return true if another student has this email, false otherwise
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Find students belonging to a specific department (case-insensitive).
     * 
     * @param department department name
     * @return list of matching students
     */
    List<Student> findByDepartmentIgnoreCase(String department);

    /**
     * Search students whose name contains the keyword (case-insensitive).
     * 
     * @param name keyword to search
     * @return list of matching students
     */
    List<Student> findByNameContainingIgnoreCase(String name);

    /**
     * Find students by year of study.
     * 
     * @param yearOfStudy year (1, 2, 3, or 4)
     * @return list of matching students
     */
    List<Student> findByYearOfStudy(Integer yearOfStudy);

    /**
     * Search students by keyword matching either name or department.
     * 
     * @param keyword search keyword
     * @return list of students where name or department matches
     */
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Student> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Paginated keyword search across name and department.
     */
    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Student> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Paginated filter by department.
     */
    Page<Student> findByDepartmentIgnoreCase(String department, Pageable pageable);
}
