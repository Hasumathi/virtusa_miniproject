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
public class SubjectResponse {

    private Long id;
    private String subjectCode;
    private String subjectName;
    private Long departmentId;
    private String departmentCode;
    private String departmentName;
    private Integer semester;
    private Integer credits;
    private LocalDateTime createdAt;
}
