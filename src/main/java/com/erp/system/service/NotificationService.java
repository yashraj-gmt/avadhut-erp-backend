package com.erp.system.service;

import com.erp.system.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    /** Returns all unread notifications for the currently authenticated user. */
    List<NotificationResponse> getUnreadForCurrentUser();

    /** Marks a single notification as read. No-op if already read or doesn't belong to the user. */
    void markAsRead(Long id);

    /**
     * Idempotent: scans all COMPLETED billing orders whose paymentDueDate is before today
     * and paymentStatus is PENDING, then creates overdue payment notifications for SUPER_ADMIN / ADMIN users.
     * Skips orders that already have a notification of type ORDER_PAYMENT_OVERDUE.
     */
    int checkAndCreateOverdueNotifications();
}
