package com.smartcampus.dto.request;

import jakarta.validation.constraints.Email;
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
public class FacultyRegisterRequest {

    @NotBlank(message = "Employee ID is mandatory")
    @Size(max = 30)
    private String employeeId;

    @NotBlank(message = "Username is mandatory")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 50)
    private String password;

    private String phone;

    @NotNull(message = "Department ID is mandatory")
    private Long departmentId;

    private String designation;

    @NotBlank(message = "Faculty authorization registration key is required")
    private String authorizationKey;
}
