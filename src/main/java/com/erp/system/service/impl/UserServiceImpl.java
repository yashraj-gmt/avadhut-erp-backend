package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateUserRequest;
import com.erp.system.dto.request.UpdateUserRequest;
import com.erp.system.dto.response.UserResponse;
import com.erp.system.entity.User;
import com.erp.system.enums.UserRole;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.UserMapper;
import com.erp.system.repository.UserRepository;
import com.erp.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final UserMapper      userMapper;
    private final PasswordEncoder passwordEncoder;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse create(CreateUserRequest request) {

        if (userRepository.existsByMobile(request.getMobile())) {
            throw new AppException("Mobile number is already in use.", HttpStatus.CONFLICT);
        }

        // Email duplicate check — only when a non-blank email is supplied
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        if (hasEmail && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email address is already in use.", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(hasEmail ? request.getEmail() : null);
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        user.setIsDeleted(false);

        User saved = userRepository.save(user);
        log.info("User created: id={}, mobile={}", saved.getId(), saved.getMobile());
        return userMapper.toResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findActive(id));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findActive(id);

        // Duplicate checks (skip if unchanged or blank)
        boolean hasEmail = request.getEmail() != null && !request.getEmail().isBlank();
        if (hasEmail
                && !request.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new AppException("Email is already in use by another account.", HttpStatus.CONFLICT);
        }
        if (request.getMobile() != null
                && !request.getMobile().equals(user.getMobile())
                && userRepository.existsByMobileAndIdNot(request.getMobile(), id)) {
            throw new AppException("Mobile is already in use by another account.", HttpStatus.CONFLICT);
        }

        user.setName(request.getName());
        user.setEmail(hasEmail ? request.getEmail().trim() : null);
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        user.setRole(request.getRole());
        user.setIsActive(request.getIsActive());

        log.info("User updated: id={}", id);
        return userMapper.toResponse(userRepository.save(user));
    }

    // ── Toggle active ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse toggleActive(Long id) {
        User user = findActive(id);
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        log.info("User {} active toggled to {}", id, user.getIsActive());
        return userMapper.toResponse(userRepository.save(user));
    }

    // ── Change role ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse changeRole(Long id, String role) {
        User user = findActive(id);
        try {
            user.setRole(UserRole.valueOf(role));
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid role: " + role, HttpStatus.BAD_REQUEST);
        }
        log.info("Role changed for user id={} → {}", id, role);
        return userMapper.toResponse(userRepository.save(user));
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findActive(id);
        user.setIsDeleted(true);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("User soft-deleted: id={}", id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private User findActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        return user;
    }
}
