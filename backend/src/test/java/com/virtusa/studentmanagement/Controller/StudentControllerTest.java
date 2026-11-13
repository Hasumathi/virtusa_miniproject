package com.virtusa.studentmanagement.Controller;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-level Integration Tests covering CRUD, Pagination, and CSV features.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void setupData() {
        studentRepository.deleteAll();

        studentRepository.save(new Student("Karan Mehra", "karan.mehra@example.com", "Computer Science", 3, "9876543210"));
        studentRepository.save(new Student("Simran Kaur", "simran.kaur@example.com", "Information Technology", 2, "9123456780"));
        studentRepository.save(new Student("Aditya Rao", "aditya.rao@example.com", "Mechanical Engineering", 4, "9812345678"));
    }

    @Test
    @DisplayName("Test Pagination: GET /api/students/page returns paginated metadata and content")
    void testGetStudentsPaginated() throws Exception {
        mockMvc.perform(get("/api/students/page?page=0&size=2&sortBy=name&direction=asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(2)));
    }

    @Test
    @DisplayName("Test CSV Export: GET /api/students/export/csv returns valid CSV stream")
    void testExportCsv() throws Exception {
        mockMvc.perform(get("/api/students/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=students.csv")))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(containsString("ID,Name,Email,Department,YearOfStudy,PhoneNumber")))
                .andExpect(content().string(containsString("karan.mehra@example.com")));
    }

    @Test
    @DisplayName("Test CSV Import: POST /api/students/import/csv imports valid CSV file")
    void testImportCsv() throws Exception {
        String csvData = "Name,Email,Department,YearOfStudy,PhoneNumber\n" +
                "Vikram Seth,vikram.seth@example.com,Civil,1,9876501234\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/students/import/csv").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount", is(1)))
                .andExpect(jsonPath("$.message", containsString("Successfully imported 1 students")));
    }
}
