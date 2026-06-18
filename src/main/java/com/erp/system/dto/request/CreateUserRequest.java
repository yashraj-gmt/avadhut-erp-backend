package com.erp.system.dto.request;

import com.erp.system.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Email(message = "Enter a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;   // optional

    @NotBlank(message = "Mobile is required")
    @Size(max = 15, message = "Mobile must not exceed 15 characters")
    private String mobile;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private Boolean isActive = true;
}
