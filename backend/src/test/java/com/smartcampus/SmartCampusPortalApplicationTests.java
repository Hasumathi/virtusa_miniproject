package com.smartcampus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SmartCampusPortalApplicationTests {

    @Test
    @DisplayName("Application Context Loads Successfully")
    void contextLoads() {
        assertTrue(true, "Spring application context loaded with all initial beans and configurations.");
    }
}
