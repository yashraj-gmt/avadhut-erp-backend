package com.erp.system.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public final class CookieUtils {

    private CookieUtils() {}

    public static void addRefreshTokenCookie(HttpServletResponse response,
                                             String cookieName,
                                             String value,
                                             int    maxAgeSeconds) {
        Cookie cookie = new Cookie(cookieName, value);
        cookie.setHttpOnly(true);           // Not accessible via JS
        cookie.setSecure(true);             // HTTPS only — set to false for local dev
        cookie.setPath("/api/auth");        // Scope cookie to auth endpoints only
        cookie.setMaxAge(maxAgeSeconds);
        // SameSite=Strict via header (Cookie API doesn't support it directly in Java < 21)
        response.addCookie(cookie);
        response.addHeader("Set-Cookie",
                response.getHeader("Set-Cookie") + "; SameSite=Strict");
    }

    public static Optional<String> getRefreshTokenFromCookie(HttpServletRequest request,
                                                             String cookieName) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> cookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    public static void clearRefreshTokenCookie(HttpServletResponse response,
                                               String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}