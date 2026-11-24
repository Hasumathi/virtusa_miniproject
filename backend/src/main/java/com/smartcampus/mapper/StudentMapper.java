package com.smartcampus.mapper;

import com.smartcampus.dto.request.StudentCreateRequest;
import com.smartcampus.dto.request.StudentUpdateRequest;
import com.smartcampus.dto.response.StudentResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public Student toEntity(StudentCreateRequest request, Department department) {
        if (request == null) {
            return null;
        }

        return Student.builder()
                .registerNumber(request.getRegisterNumber().trim().toUpperCase())
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .department(department)
                .year(request.getYear())
                .semester(request.getSemester())
                .section(request.getSection() != null ? request.getSection().trim().toUpperCase() : null)
                .dateOfJoining(request.getDateOfJoining())
                .profileImageUrl(request.getProfileImageUrl())
                .isDeleted(false)
                .build();
    }

    public void updateEntityFromRequest(Student student, StudentUpdateRequest request, Department department) {
        if (student == null || request == null) {
            return;
        }

        student.setFullName(request.getFullName().trim());
        student.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        student.setDepartment(department);
        student.setYear(request.getYear());
        student.setSemester(request.getSemester());
        student.setSection(request.getSection() != null ? request.getSection().trim().toUpperCase() : null);
        student.setDateOfJoining(request.getDateOfJoining());
        if (request.getProfileImageUrl() != null) {
            student.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getAccountStatus() != null) {
            student.setAccountStatus(request.getAccountStatus());
        }
    }

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }

        return StudentResponse.builder()
                .id(student.getId())
                .registerNumber(student.getRegisterNumber())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .phone(student.getPhone())
                .departmentId(student.getDepartment() != null ? student.getDepartment().getId() : null)
                .departmentCode(student.getDepartment() != null ? student.getDepartment().getCode() : null)
                .departmentName(student.getDepartment() != null ? student.getDepartment().getName() : null)
                .year(student.getYear())
                .semester(student.getSemester())
                .section(student.getSection())
                .dateOfJoining(student.getDateOfJoining())
                .profileImageUrl(student.getProfileImageUrl())
                .accountStatus(student.getAccountStatus())
                .userId(student.getUser() != null ? student.getUser().getId() : null)
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}
