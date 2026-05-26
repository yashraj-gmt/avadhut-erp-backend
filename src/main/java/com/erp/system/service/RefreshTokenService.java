package com.erp.system.service;

import com.erp.system.entity.RefreshToken;
import com.erp.system.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);

    RefreshToken validateAndRotate(String token, String ipAddress, String userAgent);

    void revokeAllUserTokens(User user);

    void cleanupExpiredTokens();
}