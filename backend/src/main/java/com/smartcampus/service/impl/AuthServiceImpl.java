package com.smartcampus.service.impl;

import com.smartcampus.dto.request.*;
import com.smartcampus.dto.response.AuthResponse;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.Faculty;
import com.smartcampus.entity.PasswordResetToken;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.User;
import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.Role;
import com.smartcampus.exception.DuplicateResourceException;
import com.smartcampus.exception.InvalidCredentialsException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.UnauthorizedOperationException;
import com.smartcampus.repository.*;
import com.smartcampus.security.UserPrincipal;
import com.smartcampus.security.jwt.JwtTokenProvider;
import com.smartcampus.service.AuditService;
import com.smartcampus.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final StudentRepository studentRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Value("${app.security.faculty-key:FACULTY_SECRET_KEY_2026}")
    private String facultyAuthorizationKey;

    @Autowired
    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            FacultyRepository facultyRepository,
            StudentRepository studentRepository,
            DepartmentRepository departmentRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.facultyRepository = facultyRepository;
        this.studentRepository = studentRepository;
        this.departmentRepository = departmentRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.auditService = auditService;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login attempt for identifier: {}", request.getUsernameOrEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail().trim(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            String token = tokenProvider.generateToken(authentication);

            Long linkedId = null;
            if (userPrincipal.getRole() == Role.FACULTY) {
                linkedId = facultyRepository.findByUserId(userPrincipal.getId())
                        .map(Faculty::getId).orElse(null);
            } else if (userPrincipal.getRole() == Role.STUDENT) {
                linkedId = studentRepository.findByUserId(userPrincipal.getId())
                        .map(Student::getId).orElse(null);
            }

            log.info("User authenticated successfully: username={}, role={}", 
                    userPrincipal.getUsername(), userPrincipal.getRole());

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .userId(userPrincipal.getId())
                    .username(userPrincipal.getUsername())
                    .email(userPrincipal.getEmail())
                    .role(userPrincipal.getRole())
                    .facultyOrStudentId(linkedId)
                    .expiresInMs(tokenProvider.getExpirationInMs())
                    .build();

        } catch (BadCredentialsException ex) {
            log.warn("Invalid login attempt for: {}", request.getUsernameOrEmail());
            throw new InvalidCredentialsException("Invalid username/email or password provided");
        }
    }

    @Override
    public AuthResponse registerFaculty(FacultyRegisterRequest request) {
        log.info("Processing faculty registration for employee ID: {}", request.getEmployeeId());

        if (!facultyAuthorizationKey.equals(request.getAuthorizationKey())) {
            log.warn("Unauthorized attempt to register faculty. Invalid authorization key.");
            throw new UnauthorizedOperationException("Invalid faculty registration authorization key");
        }

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email) || facultyRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User/Faculty", "email", email);
        }

        String employeeId = request.getEmployeeId().trim().toUpperCase();
        if (facultyRepository.existsByEmployeeId(employeeId)) {
            throw new DuplicateResourceException("Faculty", "employeeId", employeeId);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.FACULTY)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        User savedUser = userRepository.save(user);

        Faculty faculty = Faculty.builder()
                .employeeId(employeeId)
                .fullName(request.getFullName().trim())
                .email(email)
                .phone(request.getPhone())
                .department(department)
                .designation(request.getDesignation())
                .user(savedUser)
                .build();
        Faculty savedFaculty = facultyRepository.save(faculty);

        log.info("Authorized faculty account created: employeeId={}, userId={}", employeeId, savedUser.getId());
        auditService.logAction(savedUser.getId(), "REGISTER_FACULTY", "FACULTY", savedFaculty.getId(),
                "Registered faculty: " + employeeId);

        String token = tokenProvider.generateTokenFromUser(
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole().name());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .facultyOrStudentId(savedFaculty.getId())
                .expiresInMs(tokenProvider.getExpirationInMs())
                .build();
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Processing password change for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed: Current password mismatch for user ID: {}", userId);
            throw new InvalidCredentialsException("Current password does not match our records");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully updated for user ID: {}", userId);

        auditService.logAction(userId, "CHANGE_PASSWORD", "USER", userId, "User changed account password");
    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Processing forgot password request for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        passwordResetTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        passwordResetTokenRepository.save(resetToken);
        log.info("Password reset token generated for user: {}", user.getUsername());

        return token;
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset using token");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid or non-existent password reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidCredentialsException("Password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        log.info("Password reset successfully executed for user: {}", user.getUsername());

        auditService.logAction(user.getId(), "RESET_PASSWORD", "USER", user.getId(), "User reset password with token");
    }

    @Override
    public void activateAccount(AccountActivationRequest request) {
        String identifier = request.getIdentifier().trim();
        log.info("Processing account activation for identifier: {}", identifier);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        User user = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", identifier));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        if (user.getRole() == Role.STUDENT) {
            studentRepository.findByUserId(user.getId()).ifPresent(student -> {
                student.setAccountStatus(AccountStatus.ACTIVE);
                studentRepository.save(student);
            });
        }

        log.info("Account successfully activated for user: {}", user.getUsername());
        auditService.logAction(user.getId(), "ACTIVATE_ACCOUNT", "USER", user.getId(), "User activated account");
    }

    @Override
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        tokenProvider.invalidateToken(token);
        log.info("User session logged out and token invalidated");
    }
}
