package com.ayrotek.reckon.gpumonitoring.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gpuMonitoringOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RECKON GPU Monitoring Service (EMS)")
                        .description("""
                                EMS server for RECKON GPU rigs.
                                Rig clients register via /api/v1/nodes/initialize, wait for admin approval, \
                                then send telemetry heartbeats with a Bearer node token. \
                                Power setpoint commands queued via /api/v1/nodes/{nodeId}/power are delivered \
                                to the rig on its next heartbeat.""")
                        .version("v1"))
                .components(new Components().addSecuritySchemes("node-token", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Node JWT issued by /api/v1/nodes/initialize (used by rig clients on /heartbeat)")));
    }
}
