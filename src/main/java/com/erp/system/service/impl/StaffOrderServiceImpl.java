package com.erp.system.service.impl;

import com.erp.system.dto.response.OrderSummaryForStaffDto;
import com.erp.system.entity.Order;
import com.erp.system.exception.AppException;
import com.erp.system.repository.OrderRepository;
import com.erp.system.service.StaffOrderService;
import com.erp.system.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffOrderServiceImpl implements StaffOrderService {

    private final OrderRepository orderRepository;

    // ── Get all assigned orders ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryForStaffDto> getMyOrders() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<Order> orders = orderRepository.findAssignedOrders(userId);
        log.debug("Staff user {} fetched {} assigned orders", userId, orders.size());
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Get a single assigned order ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderSummaryForStaffDto getMyOrderById(Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderRepository.findAssignedOrderById(orderId, userId)
                .orElseThrow(() -> new AppException(
                        "Order not found or you do not have permission to view it.",
                        HttpStatus.FORBIDDEN));
        return toDto(order);
    }

    // ── Count assigned orders ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getMyOrderCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        return orderRepository.countAssignedOrders(userId);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private OrderSummaryForStaffDto toDto(Order o) {
        return OrderSummaryForStaffDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .customerName(o.getCustomer() != null ? o.getCustomer().getName() : null)
                .customerMobile(o.getCustomer() != null ? o.getCustomer().getMobile() : null)
                .orderStatus(o.getOrderStatus())
                .deliveryDate(o.getDeliveryDate())
                .finalAmount(o.getFinalAmount())
                .notes(o.getNotes())
                .assignedByName(o.getCreatedBy() != null ? o.getCreatedBy().getName() : null)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
