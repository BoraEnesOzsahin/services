package com.ayrotek.reckon.hiveosintegration.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Per-client Feign configuration for the HiveOS API.
 * Not annotated with @Configuration on purpose: it is referenced from
 * @FeignClient(configuration = ...) so the auth interceptor applies only to that client.
 */
public class HiveosFeignConfig {

    @Value("${hiveos.api.token}")
    private String apiToken;

    @Bean
    public RequestInterceptor hiveosAuthInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + apiToken);
    }

    @Bean
    public Logger.Level hiveosFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public ErrorDecoder hiveosFeignErrorDecoder() {
        return new HiveosFeignErrorDecoder();
    }
}
