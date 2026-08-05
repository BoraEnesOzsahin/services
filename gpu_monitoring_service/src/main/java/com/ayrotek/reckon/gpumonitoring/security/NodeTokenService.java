package com.ayrotek.reckon.gpumonitoring.security;

import com.ayrotek.reckon.gpumonitoring.exception.InvalidNodeTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Issues and validates JWTs for GPU nodes (rigs).
 * The Python client stores the token in secrets.json and sends it as a Bearer header
 * on every heartbeat. On 401 it wipes its secrets and re-registers, so expired tokens
 * are self-healing as long as the node stays APPROVED.
 */
@Service
public class NodeTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    @Value("${node-token.secret}")
    private String secret;

    @Value("${node-token.expiration}")
    private long expirationMs;

    private SecretKey key;

    @PostConstruct
    void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String nodeId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(nodeId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public String extractNodeId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidNodeTokenException("Missing or malformed Authorization header");
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidNodeTokenException("Invalid or expired node token");
        }
    }
}
