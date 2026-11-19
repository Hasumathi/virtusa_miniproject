package com.smartcampus.service;

import com.smartcampus.dto.request.StudentCreateRequest;
import com.smartcampus.dto.request.StudentUpdateRequest;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.enums.AccountStatus;

public interface StudentService {

    StudentResponse createStudent(StudentCreateRequest request);

    StudentResponse getStudentById(Long id);

    StudentResponse getStudentByRegisterNumber(String registerNumber);

    PagedResponse<StudentResponse> getAllStudents(
            String search,
            Long departmentId,
            Integer year,
            Integer semester,
            String section,
            AccountStatus status,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    StudentResponse updateStudent(Long id, StudentUpdateRequest request);

    void deleteStudent(Long id);

    StudentResponse updateStudentStatus(Long id, AccountStatus status);
}
