package com.ayrotek.reckon.auth.service;

import com.ayrotek.reckon.auth.entity.Token;
import com.ayrotek.reckon.auth.entity.User;
import com.ayrotek.reckon.auth.exception.TokenException;
import com.ayrotek.reckon.auth.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Transactional
    public void saveForUser(User user, String rawToken, LocalDateTime expiresAt) {
        tokenRepository.revokeAllUserTokens(user.getId());
        tokenRepository.save(Token.builder()
                .tokenHash(hash(rawToken))
                .revoked(false)
                .expiresAt(expiresAt)
                .user(user)
                .build());
    }

    @Transactional
    public Token validateAndGet(String rawToken) {
        String tokenHash = hash(rawToken);
        Token token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new TokenException("Refresh token not found"));

        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenException("Refresh token is expired or revoked");
        }
        return token;
    }

    @Transactional
    public void revoke(String rawToken) {
        tokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            tokenRepository.save(token);
        });
    }
}
