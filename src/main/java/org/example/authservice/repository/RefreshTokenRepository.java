package org.example.authservice.repository;

import org.example.authservice.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByCardNumberAndExpiredFalse(Long cardNumber);
    Optional<RefreshToken> findByCardNumberAndToken(Long cardNumber,String token);
}
