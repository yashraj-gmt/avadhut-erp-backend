package com.erp.system.repository;

import com.erp.system.entity.User;
import com.erp.system.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ── Mobile-based lookups (primary auth identifier) ────────────────────

    Optional<User> findByMobile(String mobile);

    Optional<User> findByMobileAndIsActiveTrue(String mobile);

    boolean existsByMobile(String mobile);

    boolean existsByMobileAndIdNot(String mobile, Long id);

    // ── Email-based lookups (kept for non-auth use cases) ─────────────────

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    // ── Common queries ────────────────────────────────────────────────────

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE User u SET u.isActive = :status WHERE u.id = :id")
    void updateActiveStatus(@Param("id") Long id, @Param("status") Boolean status);

    long countByRole(UserRole role);
}