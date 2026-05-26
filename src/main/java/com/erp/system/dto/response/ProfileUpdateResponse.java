package com.erp.system.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileUpdateResponse {

    private UserResponse user;

    /**
     * True when the mobile number was changed.
     * The client MUST logout and re-authenticate because:
     *   • The JWT subject (mobile) no longer matches the stored value.
     *   • All refresh tokens have been revoked server-side.
     */
    private boolean mobileChanged;

    /**
     * Human-readable message to display when mobileChanged = true.
     * e.g. "Mobile number updated. Please log in again with your new number."
     */
    private String  reAuthMessage;
}