package com.smartcampus.service;

import com.smartcampus.dto.response.FacultyDashboardResponse;
import com.smartcampus.dto.response.StudentDashboardResponse;

public interface DashboardService {

    FacultyDashboardResponse getFacultyDashboard();

    StudentDashboardResponse getStudentDashboard(Long userId);
}
