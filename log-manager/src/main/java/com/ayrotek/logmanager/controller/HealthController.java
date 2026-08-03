package com.ayrotek.logmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Simple liveness endpoint.
 * Every request produces an INFO log that Filebeat picks up and ships
 * through Logstash → Elasticsearch → Kibana.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/status")
    public Map<String, Object> status() {
        log.info("Status endpoint called");
        return Map.of(
                "service", "log-manager",
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
