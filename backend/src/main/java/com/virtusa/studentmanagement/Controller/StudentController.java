package com.virtusa.studentmanagement.Controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.service.StudentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
@Tag(name = "Student Management", description = "REST APIs for Student CRUD operations, search, and department/year filters")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // GET: Retrieve all students
    @Operation(summary = "Get all students", description = "Retrieves a list of all registered students")
    @ApiResponse(responseCode = "200", description = "List of students successfully retrieved")
    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        logger.info("REST GET /api/students received");
        List<Student> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    // GET: Retrieve students with pagination and sorting
    @Operation(summary = "Get students with pagination and sorting", description = "Retrieves a paginated and sorted list of students (e.g. ?page=0&size=10&sortBy=name&direction=asc)")
    @ApiResponse(responseCode = "200", description = "Paginated list of students retrieved")
    @GetMapping("/page")
    public ResponseEntity<Page<Student>> getStudentsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        logger.info("REST GET /api/students/page received (page: {}, size: {}, sortBy: {}, direction: {})", page, size, sortBy, direction);
        Page<Student> studentPage = studentService.getStudentsPaginated(page, size, sortBy, direction);
        return ResponseEntity.ok(studentPage);
    }

    // GET: Retrieve a student by ID
    @Operation(summary = "Get student by ID", description = "Retrieves details of a specific student by their unique ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student found"),
        @ApiResponse(responseCode = "404", description = "Student not found with specified ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        logger.info("REST GET /api/students/{} received", id);
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    // POST: Add a new student
    @Operation(summary = "Create a new student", description = "Registers a new student record after validating input fields and email uniqueness")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student successfully created"),
        @ApiResponse(responseCode = "400", description = "Validation failed for request body"),
        @ApiResponse(responseCode = "409", description = "Student with this email already exists")
    })
    @PostMapping
    public ResponseEntity<Student> createStudent(@Valid @RequestBody Student student) {
        logger.info("REST POST /api/students received for email: {}", student.getEmail());
        Student createdStudent = studentService.createStudent(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    // PUT: Update an existing student
    @Operation(summary = "Update student by ID", description = "Updates details of an existing student record")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student successfully updated"),
        @ApiResponse(responseCode = "400", description = "Validation failed for request body"),
        @ApiResponse(responseCode = "404", description = "Student not found with specified ID"),
        @ApiResponse(responseCode = "409", description = "Email is already taken by another student")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody Student student) {
        logger.info("REST PUT /api/students/{} received", id);
        Student updatedStudent = studentService.updateStudent(id, student);
        return ResponseEntity.ok(updatedStudent);
    }

    // DELETE: Delete a student
    @Operation(summary = "Delete student by ID", description = "Deletes a student record from the database by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Student deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Student not found with specified ID")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        logger.info("REST DELETE /api/students/{} received", id);
        studentService.deleteStudent(id);
        return ResponseEntity.ok("Student with ID " + id + " deleted successfully");
    }

    // GET: Search students by name or department
    @Operation(summary = "Search students", description = "Search students by keyword matching either name or department (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Matching students returned")
    @GetMapping("/search")
    public ResponseEntity<List<Student>> searchStudents(@RequestParam String keyword) {
        logger.info("REST GET /api/students/search?keyword={} received", keyword);
        return ResponseEntity.ok(studentService.searchStudents(keyword));
    }

    // GET: Filter students by department
    @Operation(summary = "Filter students by department", description = "Retrieves all students belonging to a specified department")
    @ApiResponse(responseCode = "200", description = "Students in department returned")
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Student>> getStudentsByDepartment(
            @PathVariable String department) {
        logger.info("REST GET /api/students/department/{} received", department);
        return ResponseEntity.ok(studentService.getStudentsByDepartment(department));
    }

    // GET: Filter students by year of study
    @Operation(summary = "Filter students by year of study", description = "Retrieves all students in a specified academic year (1-4)")
    @ApiResponse(responseCode = "200", description = "Students in year returned")
    @GetMapping("/year/{year}")
    public ResponseEntity<List<Student>> getStudentsByYear(@PathVariable Integer year) {
        logger.info("REST GET /api/students/year/{} received", year);
        return ResponseEntity.ok(studentService.getStudentsByYear(year));
    }
}
