package com.erp.system.service.impl;

import com.erp.system.config.JwtProperties;
import com.erp.system.entity.RefreshToken;
import com.erp.system.entity.User;
import com.erp.system.exception.TokenException;
import com.erp.system.repository.RefreshTokenRepository;
import com.erp.system.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties          jwtProperties;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        return refreshTokenRepository.save(token);
    }

    /**
     * Validates the token and implements token rotation:
     * revokes the old token and issues a brand-new one.
     */
    @Override
    @Transactional
    public RefreshToken validateAndRotate(String token, String ipAddress, String userAgent) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenException("Refresh token not found. Please log in again."));

        if (existingToken.isRevoked()) {
            // Potential token reuse — revoke all tokens for this user (security measure)
            log.warn("Reuse of revoked refresh token detected for user id={}. " +
                    "Revoking all tokens.", existingToken.getUser().getId());
            revokeAllUserTokens(existingToken.getUser());
            throw new TokenException("Refresh token has been revoked. Please log in again.");
        }

        if (existingToken.isExpired()) {
            throw new TokenException("Refresh token has expired. Please log in again.");
        }

        // Revoke the used token (rotation)
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        // Issue a new refresh token
        return createRefreshToken(existingToken.getUser(), ipAddress, userAgent);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Revoked all refresh tokens for user id={}", user.getId());
    }

    /**
     * Runs nightly at 02:00 to purge expired and revoked tokens.
     */
    @Override
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredAndRevokedTokens(Instant.now());
        log.info("Cleaned up {} expired/revoked refresh tokens", deleted);
    }
}