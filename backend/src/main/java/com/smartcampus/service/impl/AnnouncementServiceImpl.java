package com.smartcampus.service.impl;

import com.smartcampus.dto.request.AnnouncementCreateRequest;
import com.smartcampus.dto.response.AnnouncementResponse;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.entity.Announcement;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedOperationException;
import com.smartcampus.repository.AnnouncementRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.service.AnnouncementService;
import com.smartcampus.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementServiceImpl.class);

    private final AnnouncementRepository announcementRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    @Autowired
    public AnnouncementServiceImpl(
            AnnouncementRepository announcementRepository,
            FacultyRepository facultyRepository,
            DepartmentRepository departmentRepository,
            AuditService auditService) {
        this.announcementRepository = announcementRepository;
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @Override
    public AnnouncementResponse createAnnouncement(Long facultyUserId, AnnouncementCreateRequest request) {
        log.info("Faculty user ID: {} creating announcement: {}", facultyUserId, request.getTitle());

        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found for user: " + facultyUserId));

        Department targetDept = null;
        if (request.getTargetDepartmentId() != null) {
            targetDept = departmentRepository.findById(request.getTargetDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getTargetDepartmentId()));
        }

        Announcement announcement = Announcement.builder()
                .faculty(faculty)
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .targetDepartment(targetDept)
                .targetYear(request.getTargetYear())
                .targetSemester(request.getTargetSemester())
                .build();

        Announcement saved = announcementRepository.save(announcement);
        log.info("Announcement saved with ID: {}", saved.getId());

        auditService.logAction(facultyUserId, "CREATE_ANNOUNCEMENT", "ANNOUNCEMENT", saved.getId(),
                "Published announcement: " + saved.getTitle());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AnnouncementResponse> getAnnouncements(
            Long departmentId, Integer year, Integer semester, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Announcement> pageResult = announcementRepository.findTargetedAnnouncements(
                departmentId, year, semester, pageable);

        List<AnnouncementResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PagedResponse.<AnnouncementResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .empty(pageResult.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AnnouncementResponse getAnnouncementById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));
        return mapToResponse(announcement);
    }

    @Override
    public void deleteAnnouncement(Long id, Long facultyUserId) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement", "id", id));

        Faculty faculty = facultyRepository.findByUserId(facultyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));

        if (!announcement.getFaculty().getId().equals(faculty.getId())) {
            throw new UnauthorizedOperationException("Cannot delete announcements authored by another faculty member");
        }

        announcementRepository.delete(announcement);
        log.info("Announcement ID: {} deleted by faculty ID: {}", id, faculty.getId());

        auditService.logAction(facultyUserId, "DELETE_ANNOUNCEMENT", "ANNOUNCEMENT", id, "Deleted announcement ID: " + id);
    }

    private AnnouncementResponse mapToResponse(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .facultyId(a.getFaculty().getId())
                .facultyName(a.getFaculty().getFullName())
                .title(a.getTitle())
                .content(a.getContent())
                .targetDepartmentId(a.getTargetDepartment() != null ? a.getTargetDepartment().getId() : null)
                .targetDepartmentName(a.getTargetDepartment() != null ? a.getTargetDepartment().getName() : null)
                .targetYear(a.getTargetYear())
                .targetSemester(a.getTargetSemester())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
