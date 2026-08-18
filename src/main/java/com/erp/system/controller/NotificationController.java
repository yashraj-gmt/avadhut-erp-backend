package com.erp.system.controller;

import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.NotificationResponse;
import com.erp.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** Get all unread notifications for the currently logged-in admin/super-admin. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread() {
        List<NotificationResponse> data = notificationService.getUnreadForCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved.", data));
    }

    /** Mark a specific notification as read. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read."));
    }

    /**
     * Trigger an idempotent scan for overdue payment orders and create notifications.
     * Called by the frontend on app load / periodically.
     */
    @PostMapping("/check-overdue")
    public ResponseEntity<ApiResponse<Integer>> checkOverdue() {
        int count = notificationService.checkAndCreateOverdueNotifications();
        return ResponseEntity.ok(ApiResponse.success("Overdue check complete. Notifications created: " + count, count));
    }
}
