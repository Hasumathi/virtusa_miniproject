package com.smartcampus.security;

import com.smartcampus.entity.Student;
import com.smartcampus.enums.Role;
import com.smartcampus.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("securityUtils")
public class SecurityUtils {

    private final StudentRepository studentRepository;

    @Autowired
    public SecurityUtils(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return Optional.empty();
        }
        return Optional.of((UserPrincipal) authentication.getPrincipal());
    }

    public Long getCurrentUserId() {
        return getCurrentUser().map(UserPrincipal::getId).orElse(null);
    }

    public String getCurrentUsername() {
        return getCurrentUser().map(UserPrincipal::getUsername).orElse(null);
    }

    public boolean hasRole(Role role) {
        return getCurrentUser().map(user -> user.getRole() == role).orElse(false);
    }

    public boolean isCurrentStudent(Long studentId) {
        if (studentId == null) {
            return false;
        }

        Optional<UserPrincipal> currentUser = getCurrentUser();
        if (currentUser.isEmpty() || currentUser.get().getRole() != Role.STUDENT) {
            return false;
        }

        Optional<Student> studentOpt = studentRepository.findByUserId(currentUser.get().getId());
        return studentOpt.isPresent() && studentOpt.get().getId().equals(studentId);
    }
}
