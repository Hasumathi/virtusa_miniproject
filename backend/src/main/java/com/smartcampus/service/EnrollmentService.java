package com.smartcampus.service;

import com.smartcampus.dto.request.BatchEnrollmentRequest;
import com.smartcampus.dto.request.EnrollmentRequest;
import com.smartcampus.dto.response.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enrollStudent(EnrollmentRequest request);

    List<EnrollmentResponse> batchEnrollStudents(BatchEnrollmentRequest request);

    List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId);

    List<EnrollmentResponse> getEnrolledStudentsBySubject(Long subjectId, String academicYear, Integer semester);

    void dropEnrollment(Long enrollmentId);
}
