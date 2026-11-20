package com.smartcampus.service;

import com.smartcampus.dto.request.SubjectCreateRequest;
import com.smartcampus.dto.request.SubjectUpdateRequest;
import com.smartcampus.dto.response.PagedResponse;
import com.smartcampus.dto.response.SubjectResponse;

import java.util.List;

public interface SubjectService {

    SubjectResponse createSubject(SubjectCreateRequest request);

    SubjectResponse getSubjectById(Long id);

    SubjectResponse getSubjectByCode(String code);

    PagedResponse<SubjectResponse> getAllSubjects(Long departmentId, Integer semester, int page, int size, String sortBy, String sortDir);

    List<SubjectResponse> getSubjectsByDepartment(Long departmentId);

    SubjectResponse updateSubject(Long id, SubjectUpdateRequest request);

    void deleteSubject(Long id);
}
