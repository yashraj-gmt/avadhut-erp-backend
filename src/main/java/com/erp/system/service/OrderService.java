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
    
    void delete(Long id);
}
