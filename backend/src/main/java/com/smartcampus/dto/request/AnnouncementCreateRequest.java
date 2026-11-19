package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementCreateRequest {

    @NotBlank(message = "Title is mandatory")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Content is mandatory")
    @Size(min = 5, max = 4000, message = "Content must be between 5 and 4000 characters")
    private String content;

    private Long targetDepartmentId;
    private Integer targetYear;
    private Integer targetSemester;
}
