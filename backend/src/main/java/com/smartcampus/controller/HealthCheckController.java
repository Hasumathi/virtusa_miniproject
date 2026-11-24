package com.smartcampus.controller;

import com.smartcampus.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "Endpoints for checking backend service and database connectivity")
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

    private final DataSource dataSource;

    @Autowired
    public HealthCheckController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    @Operation(summary = "Check backend service status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceHealth() {
        log.info("Health check ping received");
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Smart Campus Portal Backend");
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(ApiResponse.success("Service is running healthy", status));
    }

    @GetMapping("/db")
    @Operation(summary = "Check PostgreSQL database connectivity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkDatabaseConnectivity() {
        log.info("Database connectivity check initiated");
        Map<String, Object> dbStatus = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            dbStatus.put("connected", true);
            dbStatus.put("databaseProductName", metaData.getDatabaseProductName());
            dbStatus.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            dbStatus.put("driverName", metaData.getDriverName());
            log.info("Database connectivity verified successfully: {} {}", 
                    metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            return ResponseEntity.ok(ApiResponse.success("Database connection successful", dbStatus));
        } catch (Exception ex) {
            log.error("Database connectivity check failed: {}", ex.getMessage());
            dbStatus.put("connected", false);
            dbStatus.put("error", ex.getMessage());
            return ResponseEntity.status(503).body(ApiResponse.error("Database connection failed: " + ex.getMessage()));
        }
    }
}
