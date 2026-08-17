package com.erp.system.mapper;

import com.erp.system.dto.request.UpdateCustomerRequest;
import com.erp.system.dto.response.*;
import com.erp.system.entity.Customer;
import com.erp.system.entity.Invoice;
import com.erp.system.entity.Order;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for Customer entities → DTOs.
 * Temporary plain-Spring implementation replacing the MapStruct abstract class
 * until the annotation-processor code-generation issue is resolved.
 */
@Component
public class CustomerMapper {

    // ── Customer → CustomerSummaryResponse ───────────────────────────────

    public CustomerSummaryResponse toSummaryResponse(Customer c) {
        if (c == null) return null;
        return CustomerSummaryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .mobile(c.getMobile())
                .area(c.getArea())
                .city(c.getCity())
                .customerType(c.getCustomerType())
                .customerStatus(c.getCustomerStatus())
                .isActive(c.getIsActive())
                .isRegular(c.getIsRegular())
                .dateJoined(c.getDateJoined())
                .createdAt(c.getCreatedAt())
                // totalOrders / totalBookings populated by service
                .build();
    }

    // ── Customer → CustomerResponse (full detail) ─────────────────────────

    public CustomerResponse toDetailResponse(Customer c) {
        if (c == null) return null;
        return CustomerResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .mobile(c.getMobile())
                .alternateMobile(c.getAlternateMobile())
                .email(c.getEmail())
                .address(c.getAddress())
                .city(c.getCity())
                .area(c.getArea())
                .pincode(c.getPincode())
                .customerType(c.getCustomerType())
                .customerStatus(c.getCustomerStatus())
                .isActive(c.getIsActive())
                .isRegular(c.getIsRegular())
                .regularSince(c.getRegularSince())
                .dateJoined(c.getDateJoined())
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                // aggregates (totalOrders, totalInvoiceAmount, etc.) set by service
                .build();
    }

    // ── Customer → CustomerProfileResponse ────────────────────────────────

    public CustomerProfileResponse toProfileResponse(Customer c) {
        if (c == null) return null;
        return CustomerProfileResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .mobile(c.getMobile())
                .alternateMobile(c.getAlternateMobile())
                .email(c.getEmail())
                .address(c.getAddress())
                .city(c.getCity())
                .area(c.getArea())
                .pincode(c.getPincode())
                .customerType(c.getCustomerType())
                .customerStatus(c.getCustomerStatus())
                .isActive(c.getIsActive())
                .isRegular(c.getIsRegular())
                .regularSince(c.getRegularSince())
                .dateJoined(c.getDateJoined())
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                // recentOrders, recentInvoices, aggregates set by service
                .build();
    }

    // ── Order → OrderSummaryDto ───────────────────────────────────────────

    public OrderSummaryDto toOrderSummary(Order o) {
        if (o == null) return null;
        return OrderSummaryDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .orderStatus(o.getOrderStatus())
                .finalAmount(o.getFinalAmount())
                .deliveryDate(o.getDeliveryDate())
                .createdAt(o.getCreatedAt())
                .build();
    }

    // ── Invoice → InvoiceSummaryDto ───────────────────────────────────────

    public InvoiceSummaryDto toInvoiceSummary(Invoice i) {
        if (i == null) return null;
        return InvoiceSummaryDto.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .paymentStatus(i.getPaymentStatus())
                .finalAmount(i.getFinalAmount())
                .pendingAmount(i.getPendingAmount())
                .dueDate(i.getDueDate())
                .invoiceDate(i.getInvoiceDate())
                .createdAt(i.getCreatedAt())
                .build();
    }

    // ── Patch update: apply non-null fields from request onto entity ───────

    public void applyUpdate(UpdateCustomerRequest req, Customer customer) {
        if (req == null) return;
        if (req.getName()            != null) customer.setName(req.getName().trim());
        if (req.getMobile()          != null) customer.setMobile(req.getMobile().trim());
        if (req.getAlternateMobile() != null) customer.setAlternateMobile(req.getAlternateMobile());
        if (req.getEmail()           != null) customer.setEmail(req.getEmail());
        if (req.getAddress()         != null) customer.setAddress(req.getAddress());
        if (req.getCity()            != null) customer.setCity(req.getCity());
        if (req.getArea()            != null) customer.setArea(req.getArea());
        if (req.getPincode()         != null) customer.setPincode(req.getPincode());
        if (req.getCustomerType()    != null) customer.setCustomerType(req.getCustomerType());
        if (req.getCustomerStatus()  != null) customer.setCustomerStatus(req.getCustomerStatus());
        if (req.getIsActive()        != null) customer.setIsActive(req.getIsActive());
        if (req.getNotes()           != null) customer.setNotes(req.getNotes());
        if (req.getDateJoined()      != null) customer.setDateJoined(req.getDateJoined());
    }
}
