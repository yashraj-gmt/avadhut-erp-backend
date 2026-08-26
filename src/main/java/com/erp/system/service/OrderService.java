package com.erp.system.service;

import com.erp.system.dto.request.CreateOrderRequest;
import com.erp.system.dto.request.UpdateOrderRequest;
import com.erp.system.dto.request.UpdateOrderBillingRequest;
import com.erp.system.dto.response.OrderResponse;
import com.erp.system.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse create(CreateOrderRequest request);
    
    PagedResponse<OrderResponse> getAll(String search, String status, Pageable pageable);
    
    OrderResponse getById(Long id);
    
    OrderResponse update(Long id, UpdateOrderRequest request);

    OrderResponse updateBilling(Long id, UpdateOrderBillingRequest request);

    OrderResponse completeBilling(Long id);

    /** Mark payment for this order as PAID. */
    OrderResponse markPaymentDone(Long id);

    /** Record a full or partial payment against this order with payment history. */
    com.erp.system.dto.response.PaymentSummaryDto recordPayment(Long id, com.erp.system.dto.request.RecordPaymentRequest request);

    /** Get all payments recorded against this order. */
    java.util.List<com.erp.system.dto.response.PaymentSummaryDto> getOrderPayments(Long id);

    /**
     * Mark generators for this order as physically returned.
     * Sets orderStatus = COMPLETED and returnedAt = now().
     * Stock is immediately released for same-day re-booking.
     * Billing is NOT affected — it can remain PENDING.
     */
    OrderResponse markAsReturned(Long id);
    
    void delete(Long id);
}
