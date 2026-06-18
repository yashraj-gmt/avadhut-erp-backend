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
public class UpdateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 100)
    private String email;

    @Size(max = 15)
    private String mobile;

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "Active status is required")
    private Boolean isActive;
}
