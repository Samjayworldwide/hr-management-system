package com.samjay.hr_management_system;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@RequiredArgsConstructor
@EnableCaching
@Slf4j
@OpenAPIDefinition(
        info = @Info(
                title = "HR Management System",
                version = "1.0",
                description = "API Documentation"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Provide JWT token. Example: `eyJhbGci...`"
)
public class HrManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrManagementSystemApplication.class, args);
    }

}
