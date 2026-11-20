package com.smartcampus.service.impl;

import com.smartcampus.dto.request.FacultyUpdateRequest;
import com.smartcampus.dto.response.FacultyResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.exception.FacultyNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.service.AuditService;
import com.smartcampus.service.FacultyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FacultyServiceImpl implements FacultyService {

    private static final Logger log = LoggerFactory.getLogger(FacultyServiceImpl.class);

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    @Autowired
    public FacultyServiceImpl(
            FacultyRepository facultyRepository,
            DepartmentRepository departmentRepository,
            AuditService auditService) {
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse getFacultyProfile(Long userId) {
        Faculty faculty = facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));
        return mapToResponse(faculty);
    }

    @Override
    @Transactional(readOnly = true)
    public FacultyResponse getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new FacultyNotFoundException(id));
        return mapToResponse(faculty);
    }

    @Override
    public FacultyResponse updateFacultyProfile(Long userId, FacultyUpdateRequest request) {
        log.info("Updating faculty profile for user ID: {}", userId);

        Faculty faculty = facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user ID: " + userId));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        faculty.setFullName(request.getFullName().trim());
        faculty.setPhone(request.getPhone());
        faculty.setDepartment(department);
        if (request.getDesignation() != null) {
            faculty.setDesignation(request.getDesignation().trim());
        }
        if (request.getProfileImageUrl() != null) {
            faculty.setProfileImageUrl(request.getProfileImageUrl().trim());
        }

        Faculty updated = facultyRepository.save(faculty);
        log.info("Faculty profile updated for ID: {}", updated.getId());

        auditService.logAction(userId, "UPDATE_FACULTY_PROFILE", "FACULTY", updated.getId(),
                "Updated faculty profile for " + updated.getEmployeeId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FacultyResponse> getAllFaculty(Long departmentId) {
        List<Faculty> list = departmentId != null
                ? facultyRepository.findByDepartmentId(departmentId)
                : facultyRepository.findAll();

        return list.stream().map(this::mapToResponse).toList();
    }

    private FacultyResponse mapToResponse(Faculty f) {
        return FacultyResponse.builder()
                .id(f.getId())
                .employeeId(f.getEmployeeId())
                .fullName(f.getFullName())
                .email(f.getEmail())
                .phone(f.getPhone())
                .departmentId(f.getDepartment() != null ? f.getDepartment().getId() : null)
                .departmentCode(f.getDepartment() != null ? f.getDepartment().getCode() : null)
                .departmentName(f.getDepartment() != null ? f.getDepartment().getName() : null)
                .designation(f.getDesignation())
                .profileImageUrl(f.getProfileImageUrl())
                .userId(f.getUser() != null ? f.getUser().getId() : null)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }
}
