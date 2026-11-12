
package com.virtusa.studentmanagement.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Get all students
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get a student by ID
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Student not found with ID: " + id));
    }

    // Create a new student
    public Student createStudent(Student student) {
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A student with this email already exists");
        }

        student.setId(null);
        return studentRepository.save(student);
    }

    // Update an existing student
    public Student updateStudent(Long id, Student updatedStudent) {
        Student existingStudent = getStudentById(id);

        if (studentRepository.existsByEmailAndIdNot(
                updatedStudent.getEmail(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Another student already uses this email");
        }

        existingStudent.setName(updatedStudent.getName());
        existingStudent.setEmail(updatedStudent.getEmail());
        existingStudent.setDepartment(updatedStudent.getDepartment());
        existingStudent.setYearOfStudy(updatedStudent.getYearOfStudy());
        existingStudent.setPhoneNumber(updatedStudent.getPhoneNumber());

        return studentRepository.save(existingStudent);
    }

    // Delete a student
    public void deleteStudent(Long id) {
        Student student = getStudentById(id);
        studentRepository.delete(student);
    }

    // Search by name or department
    public List<Student> searchStudents(String keyword) {
        return studentRepository.searchByKeyword(keyword);
    }

    // Filter by department
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartmentIgnoreCase(department);
    }

    // Filter by year of study
    public List<Student> getStudentsByYear(Integer year) {
        return studentRepository.findByYearOfStudy(year);
    }
}
