package com.smartcampus.dto.response;

import com.smartcampus.enums.ThemePreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {

    private Long id;
    private Long userId;
    private ThemePreference themePreference;
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
}
