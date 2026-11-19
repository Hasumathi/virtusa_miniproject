package com.smartcampus.service.impl;

import com.smartcampus.dto.request.UserSettingsRequest;
import com.smartcampus.dto.response.UserSettingsResponse;
import com.smartcampus.entity.User;
import com.smartcampus.entity.UserSettings;
import com.smartcampus.enums.ThemePreference;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.repository.UserSettingsRepository;
import com.smartcampus.service.UserSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserSettingsServiceImpl(
            UserSettingsRepository userSettingsRepository,
            UserRepository userRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserSettingsResponse getUserSettings(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultSettings(userId));
        return mapToResponse(settings);
    }

    @Override
    public UserSettingsResponse updateUserSettings(Long userId, UserSettingsRequest request) {
        log.info("Updating user preferences for user ID: {}", userId);

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    return UserSettings.builder().user(user).build();
                });

        settings.setThemePreference(request.getThemePreference());
        settings.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        settings.setPushNotificationsEnabled(request.getPushNotificationsEnabled());

        UserSettings saved = userSettingsRepository.save(settings);
        log.info("Preferences saved for user ID: {}", userId);

        return mapToResponse(saved);
    }

    private UserSettings createDefaultSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserSettings settings = UserSettings.builder()
                .user(user)
                .themePreference(ThemePreference.SYSTEM)
                .emailNotificationsEnabled(true)
                .pushNotificationsEnabled(true)
                .build();

        return userSettingsRepository.save(settings);
    }

    private UserSettingsResponse mapToResponse(UserSettings s) {
        return UserSettingsResponse.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .themePreference(s.getThemePreference())
                .emailNotificationsEnabled(s.getEmailNotificationsEnabled())
                .pushNotificationsEnabled(s.getPushNotificationsEnabled())
                .build();
    }
}
