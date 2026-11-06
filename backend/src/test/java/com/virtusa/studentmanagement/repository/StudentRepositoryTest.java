package com.virtusa.studentmanagement.repository;

import com.virtusa.studentmanagement.entity.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository and Database Operations Integration Tests.
 * 
 * Member 3 Tasks 6 & 7:
 * - Implement database operations.
 * - Test database connectivity and repository methods.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class StudentRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();

        testStudent = new Student(
            "Rahul Sharma",
            "rahul.sharma@example.com",
            "Computer Science",
            3,
            "9876543210"
        );
    }

    @Test
    @DisplayName("Test 1: Save Student (Database CREATE operation)")
    void testSaveStudent() {
        Student savedStudent = studentRepository.save(testStudent);

        assertThat(savedStudent).isNotNull();
        assertThat(savedStudent.getId()).isNotNull();
        assertThat(savedStudent.getEmail()).isEqualTo("rahul.sharma@example.com");
    }

    @Test
    @DisplayName("Test 2: Find Student By ID and Email (Database READ operation)")
    void testFindByIdAndEmail() {
        Student savedStudent = studentRepository.save(testStudent);

        Optional<Student> foundById = studentRepository.findById(savedStudent.getId());
        assertThat(foundById).isPresent();
        assertThat(foundById.get().getName()).isEqualTo("Rahul Sharma");

        Optional<Student> foundByEmail = studentRepository.findByEmail("rahul.sharma@example.com");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getDepartment()).isEqualTo("Computer Science");
    }

    @Test
    @DisplayName("Test 3: Update Student Details (Database UPDATE operation)")
    void testUpdateStudent() {
        Student savedStudent = studentRepository.save(testStudent);

        savedStudent.setDepartment("Information Technology");
        savedStudent.setYearOfStudy(4);
        Student updatedStudent = studentRepository.save(savedStudent);

        assertThat(updatedStudent.getDepartment()).isEqualTo("Information Technology");
        assertThat(updatedStudent.getYearOfStudy()).isEqualTo(4);
    }

    @Test
    @DisplayName("Test 4: Delete Student By ID (Database DELETE operation)")
    void testDeleteStudent() {
        Student savedStudent = studentRepository.save(testStudent);
        Long studentId = savedStudent.getId();

        studentRepository.deleteById(studentId);

        Optional<Student> deletedStudent = studentRepository.findById(studentId);
        assertThat(deletedStudent).isEmpty();
    }

    @Test
    @DisplayName("Test 5: Verify existsByEmail for Duplicate Email Detection")
    void testExistsByEmail() {
        studentRepository.save(testStudent);

        boolean exists = studentRepository.existsByEmail("rahul.sharma@example.com");
        boolean notExists = studentRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Test 6: Filter by Department and Search by Keyword")
    void testFilterAndSearch() {
        studentRepository.save(testStudent);
        studentRepository.save(new Student("Priya Nair", "priya.nair@example.com", "Mechanical", 2, "9123456789"));

        List<Student> csStudents = studentRepository.findByDepartmentIgnoreCase("Computer Science");
        assertThat(csStudents).hasSize(1);
        assertThat(csStudents.get(0).getName()).isEqualTo("Rahul Sharma");

        List<Student> searchResults = studentRepository.searchByKeyword("priya");
        assertThat(searchResults).hasSize(1);
        assertThat(searchResults.get(0).getEmail()).isEqualTo("priya.nair@example.com");
    }
}
