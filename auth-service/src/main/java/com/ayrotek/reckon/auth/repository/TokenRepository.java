package com.ayrotek.reckon.auth.repository;

import com.ayrotek.reckon.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {

    Optional<Token> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE Token t SET t.revoked = true WHERE t.user.id = :userId AND t.revoked = false")
    void revokeAllUserTokens(String userId);
}
