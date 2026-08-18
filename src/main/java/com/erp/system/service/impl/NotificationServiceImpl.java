package com.erp.system.service.impl;

import com.erp.system.dto.response.NotificationResponse;
import com.erp.system.entity.Notification;
import com.erp.system.entity.Order;
import com.erp.system.entity.User;
import com.erp.system.enums.BillingStatus;
import com.erp.system.enums.NotificationChannel;
import com.erp.system.enums.PaymentStatus;
import com.erp.system.enums.UserRole;
import com.erp.system.repository.NotificationRepository;
import com.erp.system.repository.OrderRepository;
import com.erp.system.repository.UserRepository;
import com.erp.system.service.NotificationService;
import com.erp.system.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final String OVERDUE_TYPE = "ORDER_PAYMENT_OVERDUE";

    private final NotificationRepository notificationRepository;
    private final OrderRepository        orderRepository;
    private final UserRepository         userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadForCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findUnreadByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getUser().getId().equals(userId)) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
    }

    /**
     * Scheduled hourly cron (runs every hour at :00) + callable on login / manually.
     * Scans for past-due completed orders, updates paymentStatus to OVERDUE,
     * and generates in-app notifications for admins.
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public int checkAndCreateOverdueNotifications() {
        LocalDate today = LocalDate.now();

        // Find all admin + super_admin users to notify
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.SUPER_ADMIN || u.getRole() == UserRole.ADMIN)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .collect(Collectors.toList());

        // Find all completed orders that are past due date and payment still pending
        List<Order> overdueOrders = orderRepository.findAll().stream()
                .filter(o -> !Boolean.TRUE.equals(o.getDeleted()))
                .filter(o -> o.getBillingStatus() == BillingStatus.COMPLETED)
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PENDING || o.getPaymentStatus() == null)
                .filter(o -> o.getPaymentDueDate() != null && o.getPaymentDueDate().isBefore(today))
                .collect(Collectors.toList());

        int created = 0;
        for (Order order : overdueOrders) {
            // Update order payment status to OVERDUE in database
            order.setPaymentStatus(PaymentStatus.OVERDUE);
            orderRepository.save(order);

            if (admins.isEmpty()) continue;

            // Idempotent — skip if notification already exists for this order
            if (notificationRepository.existsByReferenceIdAndReferenceType(order.getId(), OVERDUE_TYPE)) {
                continue;
            }

            String clientName = order.getCustomer() != null ? order.getCustomer().getName() : "Unknown Client";
            String billNo = order.getBillNumber() != null ? "#" + order.getBillNumber() : "(Order " + order.getOrderNumber() + ")";
            String dueDate = order.getPaymentDueDate().toString();

            String title = "Payment Overdue: " + clientName;
            String message = String.format(
                "Payment for bill %s (Client: %s) was due on %s and is still pending. Please follow up.",
                billNo, clientName, dueDate
            );

            for (User admin : admins) {
                Notification notif = new Notification();
                notif.setUser(admin);
                notif.setTitle(title);
                notif.setMessage(message);
                notif.setChannel(NotificationChannel.SYSTEM);
                notif.setIsRead(false);
                notif.setReferenceId(order.getId());
                notif.setReferenceType(OVERDUE_TYPE);
                notificationRepository.save(notif);
                created++;
            }
        }

        if (created > 0) {
            log.info("Created {} overdue payment notifications for {} orders.", created, overdueOrders.size());
        }
        return created;
    }

    private NotificationResponse toDto(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
