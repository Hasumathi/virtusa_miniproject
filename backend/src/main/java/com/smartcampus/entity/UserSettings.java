package com.smartcampus.entity;

import com.smartcampus.enums.ThemePreference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ThemePreference themePreference = ThemePreference.SYSTEM;

    @Column(nullable = false)
    @Builder.Default
    private Boolean emailNotificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean pushNotificationsEnabled = true;
}
