package com.smartcampus.dto.request;

import com.smartcampus.enums.ThemePreference;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsRequest {

    @NotNull(message = "Theme preference is required (LIGHT, DARK, SYSTEM)")
    private ThemePreference themePreference;

    @NotNull(message = "Email notification preference is required")
    private Boolean emailNotificationsEnabled;

    @NotNull(message = "Push notification preference is required")
    private Boolean pushNotificationsEnabled;
}
