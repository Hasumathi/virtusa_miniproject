package com.smartcampus.service;

import com.smartcampus.dto.request.FacultyUpdateRequest;
import com.smartcampus.dto.response.FacultyResponse;

import java.util.List;

public interface FacultyService {

    FacultyResponse getFacultyProfile(Long userId);

    FacultyResponse getFacultyById(Long id);

    FacultyResponse updateFacultyProfile(Long userId, FacultyUpdateRequest request);

    List<FacultyResponse> getAllFaculty(Long departmentId);
}
