package com.erp.system.service;

import com.erp.system.dto.request.CreateCustomerRequest;
import com.erp.system.dto.request.CustomerFilterRequest;
import com.erp.system.dto.request.RegularCustomerConfigRequest;
import com.erp.system.dto.request.UpdateCustomerRequest;
import com.erp.system.dto.response.*;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    // ── CRUD ──────────────────────────────────────────────────────────────

    /** Create a new customer. Returns the full detail response. */
    CustomerResponse create(CreateCustomerRequest request);

    /** Patch-update a customer (only non-null fields applied). */
    CustomerResponse update(Long id, UpdateCustomerRequest request);

    /** Soft-delete a customer (sets deleted = true). */
    void softDelete(Long id);

    /** Fetch full detail for a single customer by ID. */
    CustomerResponse getById(Long id);

    // ── List / Search ─────────────────────────────────────────────────────

    /**
     * Paginated list with optional keyword search and enum/flag filters.
     * Uses a JPQL query (same as list-all endpoint).
     */
    PagedResponse<CustomerSummaryResponse> listAll(CustomerFilterRequest filter, Pageable pageable);

    /**
     * Spec/criteria-backed search — supports all filter combinations cleanly.
     * Used by the dedicated search endpoint.
     */
    PagedResponse<CustomerSummaryResponse> searchAndFilter(CustomerFilterRequest filter, Pageable pageable);

    // ── Rich Profile ──────────────────────────────────────────────────────

    /**
     * Full customer profile: basic info + recent orders/invoices + aggregates
     * (totalBusinessValue, outstandingDues, etc.).
     */
    CustomerProfileResponse getProfile(Long id);

    // ── Area-wise ─────────────────────────────────────────────────────────

    /**
     * Customers grouped by area, with per-area count summary.
     * Optionally filtered by city.
     */
    PagedResponse<AreaCustomerSummary> listByArea(String city, Pageable pageable);

    // ── Regular listing by booking count ─────────────────────────────────

    /**
     * Active customers ordered by total booking/order count descending
     * (most frequent bookers first).
     */
    PagedResponse<CustomerSummaryResponse> listByBookingCount(Pageable pageable);

    // ── Pending Payments ──────────────────────────────────────────────────

    /**
     * Customers with outstanding/unpaid invoices.
     * Sortable by totalDueAmount or maxOverdueDays.
     */
    PagedResponse<PendingPaymentResponse> listPendingPayments(Pageable pageable);

    // ── History Timeline ──────────────────────────────────────────────────

    /**
     * Chronological activity timeline for a customer: orders, invoices, payments.
     * Merged and sorted newest-first, paginated.
     */
    PagedResponse<CustomerHistoryEntry> getHistory(Long customerId, Pageable pageable);

    // ── Regular Customer Algorithm ────────────────────────────────────────

    /**
     * On-demand recalculation of the isRegular flag for all active customers.
     * Config values from the request body override application.properties defaults.
     * When {@code config} is null, property defaults are used.
     */
    RegularCustomerRecalcResult recalculateRegularCustomers(RegularCustomerConfigRequest config);
}
