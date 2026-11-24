package com.smartcampus.service.impl;

import com.smartcampus.dto.request.SubjectCreateRequest;
import com.smartcampus.dto.request.SubjectUpdateRequest;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.SubjectResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Subject;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.AuditService;
import com.smartcampus.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SubjectServiceImpl implements SubjectService {

    private static final Logger log = LoggerFactory.getLogger(SubjectServiceImpl.class);

    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    @Autowired
    public SubjectServiceImpl(
            SubjectRepository subjectRepository,
            DepartmentRepository departmentRepository,
            AuditService auditService) {
        this.subjectRepository = subjectRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @Override
    public SubjectResponse createSubject(SubjectCreateRequest request) {
        String code = request.getSubjectCode().trim().toUpperCase();
        log.info("Creating new subject with code: {}", code);

        if (subjectRepository.existsBySubjectCode(code)) {
            log.warn("Subject code already exists: {}", code);
            throw new DuplicateResourceException("Subject", "subjectCode", code);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        Subject subject = Subject.builder()
                .subjectCode(code)
                .subjectName(request.getSubjectName().trim())
                .department(department)
                .semester(request.getSemester())
                .credits(request.getCredits())
                .build();

        Subject saved = subjectRepository.save(subject);
        log.info("Subject successfully created: id={}, code={}", saved.getId(), code);

        auditService.logAction(null, "CREATE_SUBJECT", "SUBJECT", saved.getId(), "Created subject " + code);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        return mapToResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectByCode(String code) {
        Subject subject = subjectRepository.findBySubjectCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "subjectCode", code));
        return mapToResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SubjectResponse> getAllSubjects(
            Long departmentId, Integer semester, int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Subject> subjectsPage;
        if (departmentId != null) {
            subjectsPage = subjectRepository.findByDepartmentId(departmentId, pageable);
        } else {
            subjectsPage = subjectRepository.findAll(pageable);
        }

        List<SubjectResponse> content = subjectsPage.getContent().stream()
                .filter(s -> semester == null || s.getSemester().equals(semester))
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<SubjectResponse>builder()
                .content(content)
                .pageNumber(subjectsPage.getNumber())
                .pageSize(subjectsPage.getSize())
                .totalElements(subjectsPage.getTotalElements())
                .totalPages(subjectsPage.getTotalPages())
                .first(subjectsPage.isFirst())
                .last(subjectsPage.isLast())
                .empty(content.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getSubjectsByDepartment(Long departmentId) {
        return subjectRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SubjectResponse updateSubject(Long id, SubjectUpdateRequest request) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        subject.setSubjectName(request.getSubjectName().trim());
        subject.setDepartment(department);
        subject.setSemester(request.getSemester());
        subject.setCredits(request.getCredits());

        Subject updated = subjectRepository.save(subject);
        log.info("Subject updated: id={}", updated.getId());

        auditService.logAction(null, "UPDATE_SUBJECT", "SUBJECT", updated.getId(), "Updated subject " + updated.getSubjectCode());

        return mapToResponse(updated);
    }

    @Override
    public void deleteSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));

        subjectRepository.delete(subject);
        log.info("Subject deleted: id={}", id);

        auditService.logAction(null, "DELETE_SUBJECT", "SUBJECT", id, "Deleted subject id=" + id);
    }

    private SubjectResponse mapToResponse(Subject subject) {
        return SubjectResponse.builder()
                .id(subject.getId())
                .subjectCode(subject.getSubjectCode())
                .subjectName(subject.getSubjectName())
                .departmentId(subject.getDepartment().getId())
                .departmentCode(subject.getDepartment().getCode())
                .departmentName(subject.getDepartment().getName())
                .semester(subject.getSemester())
                .credits(subject.getCredits())
                .createdAt(subject.getCreatedAt())
                .build();
    }
}
