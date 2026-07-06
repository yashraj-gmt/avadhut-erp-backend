package com.erp.system.controller;

import com.erp.system.dto.request.CreateUserRequest;
import com.erp.system.dto.request.UpdateUserRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.UserResponse;
import com.erp.system.entity.User;
import com.erp.system.exception.AppException;
import com.erp.system.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for user management (admin panel).
 * All endpoints under /api/admin/** already require ADMIN or SUPER_ADMIN
 * (enforced by SecurityConfig). Role-change and delete operations are
 * additionally restricted to SUPER_ADMIN via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── GET /api/admin/users ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully.", userService.getAll())
        );
    }

    // ── GET /api/admin/users/{id} ────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully.", userService.getById(id))
        );
    }

    // ── POST /api/admin/users ────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse data = userService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully.", data));
    }

    // ── PUT /api/admin/users/{id} ────────────────────────────────────────
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully.", userService.update(id, request))
        );
    }

    // ── PATCH /api/admin/users/{id}/toggle-active ─────────────────────────
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(
            @PathVariable Long id,
            Authentication authentication) {

        // Prevent a logged-in user from deactivating their own account
        Object principal = authentication.getPrincipal();
        if (principal instanceof User loggedIn && loggedIn.getId().equals(id)) {
            throw new AppException(
                    "You cannot activate or deactivate your own account.",
                    HttpStatus.FORBIDDEN
            );
        }

        return ResponseEntity.ok(
                ApiResponse.success("User status updated.", userService.toggleActive(id))
        );
    }

    // ── PATCH /api/admin/users/{id}/role ──────────────────────────────────
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String role = body.get("role");
        return ResponseEntity.ok(
                ApiResponse.success("Role updated successfully.", userService.changeRole(id, role))
        );
    }

    // ── DELETE /api/admin/users/{id} ──────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully."));
    }
}
