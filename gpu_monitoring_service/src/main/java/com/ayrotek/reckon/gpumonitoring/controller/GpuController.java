package com.ayrotek.reckon.gpumonitoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/v1/gpu")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "gpu", description = "Proxied GPU operations")
public class GpuController {

    private final RestTemplate restTemplate;

    @Operation(summary = "Get GPU nodes from external service",
            description = "Proxies the request to an external service and returns its response directly.")
    @GetMapping("/nodes")
    public ResponseEntity<String> getGpuNodes() {
        String url = "http://147.228.93.216/api/v1/gpu/nodes";
        log.info("Forwarding GET request to {}", url);
        try {
            // Forward the request and return the response entity as is
            return restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        } catch (Exception e) {
            log.error("Failed to forward GET request to {}", url, e);
            return ResponseEntity.internalServerError().body("Failed to fetch data from external service: " + e.getMessage());
        }
    }
}
