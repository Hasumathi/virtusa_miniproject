package com.smartcampus.service;

import com.smartcampus.dto.request.*;
import com.smartcampus.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse registerFaculty(FacultyRegisterRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    String forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void activateAccount(AccountActivationRequest request);

    void logout(String token);
}
