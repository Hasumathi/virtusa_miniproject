package com.smartcampus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyUpdateRequest {

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, max = 100)
    private String fullName;

    private String phone;

    @NotNull(message = "Department ID is mandatory")
    private Long departmentId;

    private String designation;

    private String profileImageUrl;
}
