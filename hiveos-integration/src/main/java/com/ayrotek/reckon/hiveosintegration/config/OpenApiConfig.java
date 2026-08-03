package com.ayrotek.reckon.hiveosintegration.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hiveosIntegrationOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RECKON HiveOS Integration Service")
                        .description("""
                                Bridge to the HiveOS API (api2.hiveos.farm/api/v2). \
                                Exposes farm/worker monitoring and remote mining control: \
                                /mining/start resolves the wallet, pool template and flight sheet on HiveOS \
                                and applies it to the selected workers; /mining/stop sends a miner stop command.""")
                        .version("v1"));
    }
}
