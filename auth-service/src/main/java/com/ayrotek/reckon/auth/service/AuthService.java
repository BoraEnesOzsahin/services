package com.ayrotek.reckon.auth.service;

import com.ayrotek.reckon.auth.dto.request.LoginRequest;
import com.ayrotek.reckon.auth.dto.request.RefreshTokenRequest;
import com.ayrotek.reckon.auth.dto.request.RegisterRequest;
import com.ayrotek.reckon.auth.dto.response.AuthResponse;
import com.ayrotek.reckon.auth.entity.Token;
import com.ayrotek.reckon.auth.entity.User;
import com.ayrotek.reckon.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.create(request);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword())
        );

        User user = userService.getById(authentication.getName());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        Token token = tokenService.validateAndGet(request.getRefreshToken());
        return buildAuthResponse(token.getUser());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        tokenService.revoke(request.getRefreshToken());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getId(), Map.of(
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "type", "user"
        ));

        String refreshToken = jwtUtil.generateToken(user.getId(), Map.of("type", "refresh"));

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000);
        tokenService.saveForUser(user, refreshToken, expiresAt);

        return AuthResponse.builder()
                .authUserId(user.getId().toString())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiration)
                .build();
    }
}
