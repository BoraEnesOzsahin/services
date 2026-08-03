package com.ayrotek.reckon.auth.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String authUserId;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
