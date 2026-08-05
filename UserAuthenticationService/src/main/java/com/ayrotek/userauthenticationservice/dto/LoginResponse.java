package com.ayrotek.userauthenticationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("expires_in") int expiresIn,
    @JsonProperty("refresh_expires_in") int refreshExpiresIn,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("scope") String scope
) {}
