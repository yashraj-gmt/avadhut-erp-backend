package com.erp.system.controller;

import com.erp.system.dto.request.ChangePasswordRequest;
import com.erp.system.dto.request.LoginRequest;
import com.erp.system.dto.request.ProfileUpdateRequest;
import com.erp.system.dto.request.RefreshTokenRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.AuthResponse;
import com.erp.system.dto.response.ProfileUpdateResponse;
import com.erp.system.dto.response.UserResponse;
import com.erp.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest  httpRequest,
            HttpServletResponse httpResponse) {

        AuthResponse authResponse = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    /**
     * POST /api/auth/refresh-token
     * Rotates the refresh token and issues a new access token.
     * Accepts the refresh token from the request body OR the HTTP-only cookie.
     * Public endpoint.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest  httpRequest,
            HttpServletResponse httpResponse) {

        String tokenValue = (request != null) ? request.getRefreshToken() : null;
        AuthResponse authResponse = authService.refreshToken(tokenValue, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authResponse));
    }

    /**
     * POST /api/auth/logout
     * Revokes all refresh tokens for the current user and clears the cookie.
     * Requires a valid access token.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest  httpRequest,
            HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    /**
     * GET /api/auth/me
     * Returns the profile of the currently authenticated user.
     * Requires a valid access token.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse profile = authService.getProfile();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", profile));
    }

    /**
     * PATCH /api/auth/profile
     * Updates the authenticated user's name, email, and mobile.
     *
     * Response includes:
     *   mobileChanged: true  → client MUST logout and re-authenticate
     *   mobileChanged: false → stays logged in; store updated user in auth state
     */
    @PatchMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfileUpdateResponse>> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            HttpServletResponse httpResponse) {

        ProfileUpdateResponse result = authService.updateProfile(request, httpResponse);

        String message = result.isMobileChanged()
                ? "Profile updated. Please log in again with your new mobile number."
                : "Profile updated successfully.";

        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    /**
     * PATCH /api/auth/change-password
     * Changes the password of the currently authenticated user.
     * Forces re-login on all devices after success.
     */
    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password changed successfully. Please log in again on all devices."));
    }
}