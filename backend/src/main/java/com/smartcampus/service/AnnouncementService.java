package com.smartcampus.service;

import com.smartcampus.dto.request.AnnouncementCreateRequest;
import com.smartcampus.dto.response.AnnouncementResponse;
import com.smartcampus.dto.response.PagedResponse;

public interface AnnouncementService {

    AnnouncementResponse createAnnouncement(Long facultyUserId, AnnouncementCreateRequest request);

    PagedResponse<AnnouncementResponse> getAnnouncements(
            Long departmentId, Integer year, Integer semester, int page, int size);

    AnnouncementResponse getAnnouncementById(Long id);

    void deleteAnnouncement(Long id, Long facultyUserId);
}
