package com.smartcampus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {

    private Long id;
    private Long facultyId;
    private String facultyName;
    private String title;
    private String content;
    private Long targetDepartmentId;
    private String targetDepartmentName;
    private Integer targetYear;
    private Integer targetSemester;
    private LocalDateTime createdAt;
}
