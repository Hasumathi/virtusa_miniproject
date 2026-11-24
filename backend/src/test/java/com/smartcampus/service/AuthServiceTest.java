package com.smartcampus.service;

import com.smartcampus.dto.request.ChangePasswordRequest;
import com.smartcampus.dto.request.LoginRequest;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.entity.User;
import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.Role;
import com.smartcampus.exception.InvalidCredentialsException;
import com.smartcampus.repository.*;
import com.smartcampus.security.UserPrincipal;
import com.smartcampus.security.jwt.JwtTokenProvider;
import com.smartcampus.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FacultyRepository facultyRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("prof_smith")
                .email("smith@campus.edu")
                .password("encoded_pass")
                .role(Role.FACULTY)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        userPrincipal = UserPrincipal.create(sampleUser);
    }

    @Test
    @DisplayName("Login succeeds with valid credentials")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("prof_smith", "password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token-xyz");
        when(tokenProvider.getExpirationInMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-xyz", response.getAccessToken());
        assertEquals("prof_smith", response.getUsername());
        assertEquals(Role.FACULTY, response.getRole());
    }

    @Test
    @DisplayName("Login throws InvalidCredentialsException on bad credentials")
    void testLogin_BadCredentials() {
        LoginRequest request = new LoginRequest("prof_smith", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Change password succeeds when current password is valid")
    void testChangePassword_Success() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("currentPass123")
                .newPassword("newPass456")
                .confirmPassword("newPass456")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("currentPass123", "encoded_pass")).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("new_encoded_pass");

        assertDoesNotThrow(() -> authService.changePassword(1L, request));
        verify(userRepository, times(1)).save(sampleUser);
        verify(auditService, times(1)).logAction(eq(1L), eq("CHANGE_PASSWORD"), eq("USER"), eq(1L), anyString());
    }

    @Test
    @DisplayName("Change password fails when new and confirm passwords do not match")
    void testChangePassword_PasswordMismatch() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("currentPass123")
                .newPassword("newPass456")
                .confirmPassword("differentPass")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("currentPass123", "encoded_pass")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(1L, request));
    }
}
