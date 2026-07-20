package com.erp.system.controller;

import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.OrderSummaryForStaffDto;
import com.erp.system.service.StaffOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for STAFF-scoped operations.
 *
 * All endpoints under /api/staff/** require ROLE_STAFF (enforced in SecurityConfig).
 * Each endpoint additionally verifies the resource belongs to the authenticated staff user.
 */
@Slf4j
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_STAFF')")
public class StaffController {

    private final StaffOrderService staffOrderService;

    // ── GET /api/staff/dashboard ─────────────────────────────────────────────
    /**
     * Returns summary statistics for the staff user's dashboard.
     * Currently returns the count of orders assigned to them.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        long totalOrders = staffOrderService.getMyOrderCount();

        Map<String, Object> stats = Map.of(
                "totalAssignedOrders", totalOrders
        );

        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully.", stats));
    }

    // ── GET /api/staff/orders ────────────────────────────────────────────────
    /**
     * Returns all generator orders assigned to the currently authenticated staff user.
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderSummaryForStaffDto>>> getMyOrders() {
        List<OrderSummaryForStaffDto> orders = staffOrderService.getMyOrders();
        return ResponseEntity.ok(ApiResponse.success(
                "Assigned orders retrieved successfully.", orders));
    }

    // ── GET /api/staff/orders/{id} ───────────────────────────────────────────
    /**
     * Returns a single order — only if it is assigned to the current staff user.
     * Returns 403 Forbidden if the order exists but belongs to a different user.
     */
    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderSummaryForStaffDto>> getMyOrderById(
            @PathVariable Long id) {
        OrderSummaryForStaffDto order = staffOrderService.getMyOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Order retrieved successfully.", order));
    }
}
