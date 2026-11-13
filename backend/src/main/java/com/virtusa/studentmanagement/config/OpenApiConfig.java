package com.virtusa.studentmanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI Configuration.
 * 
 * Provides interactive API documentation available at:
 * - Swagger UI: http://localhost:8080/swagger-ui/index.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Management System REST API")
                        .description("REST API documentation for Student Management Backend - Team 8 Mini Project")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Virtusa Student Management Team")
                                .email("studentmanagement@virtusa.com"))
                        .license(new License().name("Apache 2.0")));
    }
}

