package com.erp.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenRequest {

    /**
     * Optional — callers can send the refresh token in the request body.
     * If absent, the filter will also check the HTTP-only cookie.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}