package com.erp.system.security;

import com.erp.system.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long   id;
    private final String name;
    private final String mobile;        // used as Spring Security "username"
    private final String email;         // retained for informational use
    private final String password;
    private final boolean active;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id          = user.getId();
        this.name        = user.getName();
        this.mobile      = user.getMobile();
        this.email       = user.getEmail();
        this.password    = user.getPassword();
        this.active      = Boolean.TRUE.equals(user.getIsActive());
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    /** Factory method */
    public static CustomUserDetails of(User user) {
        return new CustomUserDetails(user);
    }

    // ── UserDetails contract ──────────────────────────────────────────────

    /**
     * Returns mobile number as the Spring Security principal identifier.
     * This is what gets stored in the JWT subject and used for token validation.
     */
    @Override
    public String getUsername()              { return mobile; }
    @Override
    public String getPassword()              { return password; }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override
    public boolean isAccountNonExpired()     { return true; }
    @Override
    public boolean isAccountNonLocked()      { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled()               { return active; }
}