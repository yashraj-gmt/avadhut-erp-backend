package com.erp.system.service;

import com.erp.system.dto.response.OrderSummaryForStaffDto;

import java.util.List;

/**
 * Service for STAFF-scoped order operations.
 * All methods operate on orders assigned to the currently authenticated staff user.
 */
public interface StaffOrderService {

    /** Returns all orders assigned to the current staff user. */
    List<OrderSummaryForStaffDto> getMyOrders();

    /** Returns a single order — only if assigned to the current staff user. */
    OrderSummaryForStaffDto getMyOrderById(Long orderId);

    /** Returns the count of orders assigned to the current staff user. */
    long getMyOrderCount();
}
