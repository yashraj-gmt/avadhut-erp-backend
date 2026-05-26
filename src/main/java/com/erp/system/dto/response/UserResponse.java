package com.erp.system.dto.response;

import com.erp.system.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long          id;
    private String        name;
    private String        email;
    private String        mobile;
    private UserRole      role;
    private Boolean       isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}