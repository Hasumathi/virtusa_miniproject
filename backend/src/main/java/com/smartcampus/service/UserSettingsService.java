package com.smartcampus.service;

import com.smartcampus.dto.request.UserSettingsRequest;
import com.smartcampus.dto.response.UserSettingsResponse;

public interface UserSettingsService {

    UserSettingsResponse getUserSettings(Long userId);

    UserSettingsResponse updateUserSettings(Long userId, UserSettingsRequest request);
}
