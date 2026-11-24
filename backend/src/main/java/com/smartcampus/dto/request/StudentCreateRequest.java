package com.smartcampus.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCreateRequest {

    @NotBlank(message = "Register number is mandatory")
    @Size(max = 30, message = "Register number must not exceed 30 characters")
    private String registerNumber;

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Must be a well-formed email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^[0-9+()\\s-]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Department ID is mandatory")
    private Long departmentId;

    @NotNull(message = "Year is mandatory")
    @Min(value = 1, message = "Year must be at least 1")
    @Max(value = 5, message = "Year must not exceed 5")
    private Integer year;

    @NotNull(message = "Semester is mandatory")
    @Min(value = 1, message = "Semester must be at least 1")
    @Max(value = 10, message = "Semester must not exceed 10")
    private Integer semester;

    @Size(max = 10, message = "Section must not exceed 10 characters")
    private String section;

    private LocalDate dateOfJoining;

    private String profileImageUrl;

    @Builder.Default
    private Boolean createAccount = true;

    private String initialPassword;
}
