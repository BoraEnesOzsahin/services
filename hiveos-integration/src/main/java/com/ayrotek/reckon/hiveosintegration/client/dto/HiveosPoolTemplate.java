package com.ayrotek.reckon.hiveosintegration.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HiveosPoolTemplate(
        String pool,
        String coin,
        Props props
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Props(
            List<Server> servers
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Server(
            String geo,
            List<String> urls,
            @JsonProperty("ssl_urls") List<String> sslUrls
    ) {
    }
}
