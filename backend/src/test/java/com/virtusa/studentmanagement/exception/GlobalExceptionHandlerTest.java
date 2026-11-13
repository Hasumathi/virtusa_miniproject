package com.virtusa.studentmanagement.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtusa.studentmanagement.entity.Student;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for GlobalExceptionHandler and Structured Error Responses.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test 404 Not Found: Return structured ErrorResponse when student ID does not exist")
    void testStudentNotFound() throws Exception {
        mockMvc.perform(get("/api/students/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", containsString("Student not found with ID: 999999")))
                .andExpect(jsonPath("$.path", is("/api/students/999999")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Test 400 Bad Request: Return structured validation errors when request body is invalid")
    void testValidationFailure() throws Exception {
        // Create an invalid student (blank name, invalid email, year = 0, invalid phone)
        Student invalidStudent = new Student("", "invalid-email-format", "", 0, "123");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidStudent)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")))
                .andExpect(jsonPath("$.path", is("/api/students")))
                .andExpect(jsonPath("$.validationErrors", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.email", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.name", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.yearOfStudy", notNullValue()))
                .andExpect(jsonPath("$.validationErrors.phoneNumber", notNullValue()));
    }

    @Test
    @DisplayName("Test 409 Conflict: Return structured ErrorResponse on duplicate email")
    void testDuplicateEmailConflict() throws Exception {
        Student student1 = new Student("First Student", "unique.test@example.com", "CS", 1, "9876543210");
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student1)))
                .andExpect(status().isCreated());

        // Try to insert second student with same email
        Student student2 = new Student("Second Student", "unique.test@example.com", "IT", 2, "9123456789");
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(student2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("unique.test@example.com")))
                .andExpect(jsonPath("$.path", is("/api/students")));
    }
}

