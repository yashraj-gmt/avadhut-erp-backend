package com.erp.system.service;

import com.erp.system.dto.request.ChangePasswordRequest;
import com.erp.system.dto.request.LoginRequest;
import com.erp.system.dto.request.ProfileUpdateRequest;
import com.erp.system.dto.response.AuthResponse;
import com.erp.system.dto.response.ProfileUpdateResponse;
import com.erp.system.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    /**
     * Authenticates the user and returns access + refresh tokens.
     * Refresh token is also set as an HTTP-only cookie.
     */
    AuthResponse login(LoginRequest request,
                       HttpServletRequest  httpRequest,
                       HttpServletResponse httpResponse);

    /**
     * Rotates the refresh token: validates the old one, issues new access + refresh tokens.
     * Accepts token from request body (for API clients) or HTTP-only cookie (for web clients).
     */
    AuthResponse refreshToken(String refreshToken,
                              HttpServletRequest  httpRequest,
                              HttpServletResponse httpResponse);

    /**
     * Revokes all refresh tokens for the current user and clears the cookie.
     */
    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    /**
     * Returns the profile of the currently authenticated user.
     */
    UserResponse getProfile();

    ProfileUpdateResponse updateProfile(ProfileUpdateRequest request,
                                        HttpServletResponse httpResponse);

    /**
     * Changes password for the currently authenticated user.
     */
    void changePassword(ChangePasswordRequest request);
}