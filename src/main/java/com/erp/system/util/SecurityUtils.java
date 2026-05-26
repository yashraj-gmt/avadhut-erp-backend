package com.erp.system.util;

import com.erp.system.exception.AppException;
import com.erp.system.security.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the currently authenticated user's CustomUserDetails.
     * Throws {@link AppException} 401 if no authenticated user exists.
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            throw new AppException("No authenticated user found.", HttpStatus.UNAUTHORIZED);
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /** Returns the mobile number of the currently authenticated user. */
    public static String getCurrentUserMobile() {
        return getCurrentUser().getMobile();
    }

    public static boolean hasRole(String role) {
        return getCurrentUser().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    public static boolean isSuperAdmin() {
        return hasRole("ROLE_SUPER_ADMIN");
    }

    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN") || isSuperAdmin();
    }
}