package com.erp.system.service;

import com.erp.system.dto.request.CreateUserRequest;
import com.erp.system.dto.request.UpdateUserRequest;
import com.erp.system.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    /** Create a new system user (SUPER_ADMIN only). */
    UserResponse create(CreateUserRequest request);

    /** Return all non-deleted users ordered by createdAt desc. */
    List<UserResponse> getAll();

    /** Return a single user by id. */
    UserResponse getById(Long id);

    /** Update name / email / mobile / role / isActive. */
    UserResponse update(Long id, UpdateUserRequest request);

    /** Toggle isActive for a user. */
    UserResponse toggleActive(Long id);

    /** Change a user's role. */
    UserResponse changeRole(Long id, String role);

    /** Soft-delete a user (sets isDeleted = true, deactivates). */
    void delete(Long id);
}
