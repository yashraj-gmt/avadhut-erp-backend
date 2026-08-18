package com.erp.system.repository;

import com.erp.system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** All unread notifications for a user, newest first. */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    /** Check if an overdue notification already exists for a given order (avoids duplicates). */
    boolean existsByReferenceIdAndReferenceType(Long referenceId, String referenceType);
}
