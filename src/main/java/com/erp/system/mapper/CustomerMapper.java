package com.erp.system.mapper;

import com.erp.system.dto.request.UpdateCustomerRequest;
import com.erp.system.dto.response.*;
import com.erp.system.entity.Customer;
import com.erp.system.entity.Invoice;
import com.erp.system.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manual mapper for Customer entities → DTOs.
 */
@Component
public class CustomerMapper {

    // ── Customer → CustomerSummaryResponse ───────────────────────────────

    public CustomerSummaryResponse toSummaryResponse(Customer c) {
        if (c == null) return null;
        return CustomerSummaryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .firmName(c.getFirmName())
                .mobile(c.getMobile())
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
                .firmName(c.getFirmName())
                .mobile(c.getMobile())
                .alternateMobile(c.getAlternateMobile())
                .email(c.getEmail())
                .address(c.getAddress())
                .addressLocationLink(c.getAddressLocationLink())
                .customerStatus(c.getCustomerStatus())
                .isActive(c.getIsActive())
                .isRegular(c.getIsRegular())
                .regularSince(c.getRegularSince())
                .dateJoined(c.getDateJoined())
                .remarks(c.getRemarks())
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
                .firmName(c.getFirmName())
                .mobile(c.getMobile())
                .alternateMobile(c.getAlternateMobile())
                .email(c.getEmail())
                .address(c.getAddress())
                .addressLocationLink(c.getAddressLocationLink())
                .customerStatus(c.getCustomerStatus())
                .isActive(c.getIsActive())
                .isRegular(c.getIsRegular())
                .regularSince(c.getRegularSince())
                .dateJoined(c.getDateJoined())
                .remarks(c.getRemarks())
                .notes(c.getNotes())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                // recentOrders, recentInvoices, aggregates set by service
                .build();
    }


    // ── Order → OrderSummaryDto ───────────────────────────────────────────

    public OrderSummaryDto toOrderSummary(Order o) {
        if (o == null) return null;

        String functionDate = "";
        if (o.getFunctionDateFrom() != null && o.getFunctionDateTo() != null) {
            functionDate = o.getFunctionDateFrom() + " to " + o.getFunctionDateTo();
        } else if (o.getDeliveryDate() != null) {
            functionDate = o.getDeliveryDate().toString();
        }

        List<PaymentSummaryDto> paymentDtos = null;
        if (o.getPayments() != null) {
            paymentDtos = o.getPayments().stream()
                    .map(this::toPaymentSummary)
                    .toList();
        }

        return OrderSummaryDto.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .billNumber(o.getBillNumber())
                .orderStatus(o.getOrderStatus())
                .billingStatus(o.getBillingStatus())
                .deliveryDate(o.getDeliveryDate())
                .functionDateFrom(o.getFunctionDateFrom())
                .functionDateTo(o.getFunctionDateTo())
                .functionDate(functionDate)
                .siteAddress(o.getSiteAddress())
                .siteAddressLink(o.getSiteAddressLink())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .taxAmount(o.getTaxAmount())
                .finalAmount(o.getFinalAmount())
                .paidAmount(o.getPaidAmount() != null ? o.getPaidAmount() : java.math.BigDecimal.ZERO)
                .pendingAmount(o.getPendingAmount() != null ? o.getPendingAmount() : o.getFinalAmount())
                .paymentStatus(o.getPaymentStatus())
                .paymentCompletionDate(o.getPaymentCompletionDate())
                .paymentDueDate(o.getPaymentDueDate())
                .payments(paymentDtos)
                .createdAt(o.getCreatedAt())
                .build();
    }

    public PaymentSummaryDto toPaymentSummary(com.erp.system.entity.Payment p) {
        if (p == null) return null;
        return PaymentSummaryDto.builder()
                .id(p.getId())
                .orderId(p.getOrder() != null ? p.getOrder().getId() : null)
                .amount(p.getAmount())
                .pendingAfterPayment(p.getPendingAfterPayment())
                .paymentMode(p.getPaymentMode())
                .paymentDate(p.getPaymentDate())
                .transactionReference(p.getTransactionReference())
                .notes(p.getNotes())
                .collectedByName(p.getCollectedBy() != null ? p.getCollectedBy().getName() : null)
                .createdAt(p.getCreatedAt())
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
        if (req.getName()                != null) customer.setName(req.getName().trim());
        if (req.getFirmName()            != null) customer.setFirmName(req.getFirmName().trim());
        if (req.getMobile()              != null) customer.setMobile(req.getMobile().trim());
        if (req.getAlternateMobile()     != null) customer.setAlternateMobile(req.getAlternateMobile());
        if (req.getEmail()               != null) customer.setEmail(req.getEmail());
        if (req.getAddress()             != null) customer.setAddress(req.getAddress());
        if (req.getAddressLocationLink() != null) customer.setAddressLocationLink(req.getAddressLocationLink());
        if (req.getRemarks()             != null) customer.setRemarks(req.getRemarks());
        if (req.getNotes()               != null) customer.setNotes(req.getNotes());
        if (req.getCustomerStatus()      != null) customer.setCustomerStatus(req.getCustomerStatus());
        if (req.getIsActive()            != null) customer.setIsActive(req.getIsActive());
        if (req.getIsRegular()           != null) customer.setIsRegular(req.getIsRegular());
        if (req.getDateJoined()          != null) customer.setDateJoined(req.getDateJoined());
    }
}
