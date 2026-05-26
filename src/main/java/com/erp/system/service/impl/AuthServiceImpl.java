package com.erp.system.service.impl;

import com.erp.system.config.JwtProperties;
import com.erp.system.dto.request.ChangePasswordRequest;
import com.erp.system.dto.request.LoginRequest;
import com.erp.system.dto.request.ProfileUpdateRequest;
import com.erp.system.dto.response.AuthResponse;
import com.erp.system.dto.response.ProfileUpdateResponse;
import com.erp.system.dto.response.UserResponse;
import com.erp.system.entity.RefreshToken;
import com.erp.system.entity.User;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.UserMapper;
import com.erp.system.repository.UserRepository;
import com.erp.system.security.CustomUserDetails;
import com.erp.system.security.JwtTokenProvider;
import com.erp.system.service.AuthService;
import com.erp.system.service.RefreshTokenService;
import com.erp.system.util.CookieUtils;
import com.erp.system.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager  authManager;
    private final JwtTokenProvider       tokenProvider;
    private final RefreshTokenService    refreshTokenService;
    private final UserRepository         userRepository;
    private final PasswordEncoder        passwordEncoder;
    private final UserMapper             userMapper;
    private final JwtProperties          jwtProperties;

    // ── Login ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse login(LoginRequest        request,
                              HttpServletRequest  httpRequest,
                              HttpServletResponse httpResponse) {

        // 1. Authenticate — Spring Security calls loadUserByUsername(mobile)
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getMobile(),    // mobile as principal
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 2. Issue JWT access token
        String accessToken = tokenProvider.generateAccessToken(userDetails);

        // 3. Create and persist refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                userRepository.getReferenceById(userDetails.getId()),
                extractIp(httpRequest),
                extractUserAgent(httpRequest)
        );

        // 4. Update last login timestamp
        userRepository.updateLastLogin(userDetails.getId(), LocalDateTime.now());

        // 5. Set refresh token in HTTP-only cookie
        setRefreshTokenCookie(httpResponse, refreshToken.getToken());

        // 6. Fetch full user entity for the response
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        log.info("User with mobile '{}' logged in successfully", request.getMobile());

        return AuthResponse.of(
                accessToken,
                jwtProperties.getAccessTokenExpirationMs(),
                refreshToken.getToken(),
                userMapper.toResponse(user)
        );
    }

    // ── Refresh Token ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse refreshToken(String             refreshTokenValue,
                                     HttpServletRequest  httpRequest,
                                     HttpServletResponse httpResponse) {

        // Accept token from body OR from HTTP-only cookie
        String tokenValue = StringUtils.hasText(refreshTokenValue)
                ? refreshTokenValue
                : CookieUtils.getRefreshTokenFromCookie(
                        httpRequest,
                        jwtProperties.getRefreshTokenCookieName())
                .orElseThrow(() -> new com.erp.system.exception.TokenException(
                        "Refresh token not provided. Please log in again."));

        // Validate + rotate (old token revoked, new token issued)
        RefreshToken newRefreshToken = refreshTokenService.validateAndRotate(
                tokenValue,
                extractIp(httpRequest),
                extractUserAgent(httpRequest)
        );

        User              user        = newRefreshToken.getUser();
        CustomUserDetails userDetails = CustomUserDetails.of(user);
        String            newAccess   = tokenProvider.generateAccessToken(userDetails);

        setRefreshTokenCookie(httpResponse, newRefreshToken.getToken());

        log.info("Tokens rotated for user with mobile '{}'", user.getMobile());

        return AuthResponse.of(
                newAccess,
                jwtProperties.getAccessTokenExpirationMs(),
                newRefreshToken.getToken(),
                userMapper.toResponse(user)
        );
    }

    // ── Logout ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
            User user = userRepository.getReferenceById(currentUser.getId());

            refreshTokenService.revokeAllUserTokens(user);

            CookieUtils.clearRefreshTokenCookie(
                    httpResponse,
                    jwtProperties.getRefreshTokenCookieName()
            );

            log.info("User with mobile '{}' logged out", currentUser.getMobile());
        } catch (Exception e) {
            log.warn("Logout called without a valid session: {}", e.getMessage());
        }
    }

    // ── Profile ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toResponse(user);
    }


    @Override
    @Transactional
    public ProfileUpdateResponse updateProfile(ProfileUpdateRequest request,
                                               HttpServletResponse   httpResponse) {

        Long userId = SecurityUtils.getCurrentUserId();
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // ── Duplicate-check: email ────────────────────────────────────────
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new AppException(
                    "Email address is already in use by another account.",
                    HttpStatus.CONFLICT);
        }

        // ── Duplicate-check: mobile ───────────────────────────────────────
        boolean mobileChanged = !user.getMobile().equals(request.getMobile());
        if (mobileChanged
                && userRepository.existsByMobileAndIdNot(request.getMobile(), userId)) {
            throw new AppException(
                    "Mobile number is already in use by another account.",
                    HttpStatus.CONFLICT);
        }

        // ── Apply changes ─────────────────────────────────────────────────
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobile(request.getMobile());
        userRepository.save(user);

        String reAuthMessage = null;

        if (mobileChanged) {
            // JWT subject is the mobile number.
            // Changing it means the current token and all refresh tokens are now invalid.
            // Revoke all sessions — client must re-authenticate with new mobile.
            refreshTokenService.revokeAllUserTokens(user);
            CookieUtils.clearRefreshTokenCookie(
                    httpResponse,
                    jwtProperties.getRefreshTokenCookieName()
            );
            reAuthMessage = "Mobile number updated. Please log in again with your new number.";
            log.info("Mobile changed for user id={}. All sessions revoked.", userId);
        }

        log.info("Profile updated for user id={}", userId);

        return ProfileUpdateResponse.builder()
                .user(userMapper.toResponse(user))
                .mobileChanged(mobileChanged)
                .reAuthMessage(reAuthMessage)
                .build();
    }

    // ── Change Password ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(
                    "New password and confirm password do not match.",
                    HttpStatus.BAD_REQUEST);
        }

        Long userId = SecurityUtils.getCurrentUserId();
        User user   = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AppException("Current password is incorrect.", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all sessions — forces re-login on every device
        refreshTokenService.revokeAllUserTokens(user);

        log.info("Password changed for user id={} (mobile={})", userId, user.getMobile());
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    private void setRefreshTokenCookie(HttpServletResponse response, String tokenValue) {
        int maxAgeSeconds = (int) (jwtProperties.getRefreshTokenExpirationMs() / 1000);
        CookieUtils.addRefreshTokenCookie(
                response,
                jwtProperties.getRefreshTokenCookieName(),
                tokenValue,
                maxAgeSeconds
        );
    }

    private String extractIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}