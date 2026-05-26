package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String      accessToken;
    private String      tokenType;
    private long        expiresIn;          // seconds until access token expires
    private String      refreshToken;       // returned in body for API clients; also set as cookie
    private UserResponse user;

    public static AuthResponse of(String accessToken,
                                  long   expiresInMs,
                                  String refreshToken,
                                  UserResponse user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresInMs / 1000)
                .refreshToken(refreshToken)
                .user(user)
                .build();
    }
}