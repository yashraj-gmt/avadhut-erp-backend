package com.erp.system.controller;

import com.erp.system.dto.request.CreateOrderRequest;
import com.erp.system.dto.request.UpdateOrderRequest;
import com.erp.system.dto.request.UpdateOrderBillingRequest;
import com.erp.system.dto.response.ApiResponse;
import com.erp.system.dto.response.OrderResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.dto.response.PaymentSummaryDto;
import com.erp.system.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse data = orderService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully.", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAll(
            @RequestParam(required = false)           String  search,
            @RequestParam(required = false)           String  status,
            @RequestParam(defaultValue = "0")         int     page,
            @RequestParam(defaultValue = "20")        int     size,
            @RequestParam(defaultValue = "createdAt") String  sortBy,
            @RequestParam(defaultValue = "desc")      String  sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PagedResponse<OrderResponse> data =
                orderService.getAll(search, status, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully.", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved successfully.", orderService.getById(id))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        OrderResponse data = orderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully.", data));
    }

    @GetMapping("/{id}/billing")
    public ResponseEntity<ApiResponse<OrderResponse>> getBilling(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Billing data retrieved successfully.", orderService.getById(id))
        );
    }

    @PutMapping("/{id}/billing")
    public ResponseEntity<ApiResponse<OrderResponse>> updateBilling(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderBillingRequest request) {
        OrderResponse data = orderService.updateBilling(id, request);
        return ResponseEntity.ok(ApiResponse.success("Billing saved successfully.", data));
    }

    @PostMapping("/{id}/billing/complete")
    public ResponseEntity<ApiResponse<OrderResponse>> completeBilling(@PathVariable Long id) {
        OrderResponse data = orderService.completeBilling(id);
        return ResponseEntity.ok(ApiResponse.success("Billing marked as completed.", data));
    }

    @PostMapping("/{id}/payment/done")
    public ResponseEntity<ApiResponse<OrderResponse>> markPaymentDone(@PathVariable Long id) {
        OrderResponse data = orderService.markPaymentDone(id);
        return ResponseEntity.ok(ApiResponse.success("Payment marked as done.", data));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<com.erp.system.dto.response.PaymentSummaryDto>> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody com.erp.system.dto.request.RecordPaymentRequest request) {
        com.erp.system.dto.response.PaymentSummaryDto data = orderService.recordPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Payment recorded successfully.", data));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<
            List<PaymentSummaryDto>>> getOrderPayments(
            @PathVariable Long id) {
        List<com.erp.system.dto.response.PaymentSummaryDto> data = orderService.getOrderPayments(id);
        return ResponseEntity.ok(ApiResponse.success("Payment history retrieved successfully.", data));
    }

    @PostMapping("/{id}/mark-returned")
    public ResponseEntity<ApiResponse<OrderResponse>> markAsReturned(@PathVariable Long id) {
        OrderResponse data = orderService.markAsReturned(id);
        return ResponseEntity.ok(ApiResponse.success("Generators marked as returned. Stock released successfully.", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully."));
    }
}