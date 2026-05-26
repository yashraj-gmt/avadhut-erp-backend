package com.erp.system.security;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String BEARER_PREFIX          = "Bearer ";
    public static final String AUTHORIZATION_HEADER   = "Authorization";
    public static final String ROLE_SUPER_ADMIN       = "ROLE_SUPER_ADMIN";
    public static final String ROLE_ADMIN             = "ROLE_ADMIN";
    public static final String ROLE_STAFF             = "ROLE_STAFF";

    // Public endpoints — no authentication required
    public static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };
}