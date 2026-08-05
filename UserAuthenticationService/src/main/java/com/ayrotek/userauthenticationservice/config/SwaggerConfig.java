package com.ayrotek.userauthenticationservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration.
 * Enables Swagger UI at /swagger-ui.html and configures JWT Bearer authentication
 * so that the token can be entered directly in the UI for authorized calls.
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the security scheme – HTTP Bearer with JWT format
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // Apply the security scheme globally to all operations
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(new Info()
                        .title("User Authentication Service API")
                        .description("API documentation with JWT authentication support.")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuth));
    }
}
