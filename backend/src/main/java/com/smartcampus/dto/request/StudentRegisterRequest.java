package com.smartcampus.dto.request;

import jakarta.validation.constraints.Email;
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
public class StudentRegisterRequest {

    @NotBlank(message = "Register number is mandatory")
    @Size(max = 30)
    private String registerNumber;

    @NotBlank(message = "Full name is mandatory")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 6, max = 50)
    private String password;

    private Long departmentId;
}
