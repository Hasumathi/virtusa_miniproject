package com.smartcampus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartCampusPortalApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartCampusPortalApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusPortalApplication.class, args);
        log.info("Smart Campus Portal Backend successfully started and ready to serve requests.");
    }
}
