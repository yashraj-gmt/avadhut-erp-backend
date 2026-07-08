package com.erp.system.mapper;

import com.erp.system.dto.response.*;
import com.erp.system.entity.Customer;
import com.erp.system.entity.Invoice;
import com.erp.system.entity.Order;
import com.erp.system.entity.Payment;
import org.mapstruct.*;

/**
 * MapStruct mapper for Customer entities → DTOs.
 *
 * <p>Uses abstract class (not interface) so we can inject repositories/services
 * for computed fields, following the same pattern as {@code ProductMapper}.
 *
 * <p>Computed fields (totalOrders, totalInvoiceAmount, outstandingDues, etc.)
 * are intentionally ignored by MapStruct and populated in {@code @AfterMapping}
 * hooks or directly in the service layer.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class CustomerMapper {

    // ── Customer → CustomerSummaryResponse ───────────────────────────────

    /**
     * Lightweight summary mapping. Computed aggregate fields (totalOrders,
     * totalBookings) are ignored here and set by the service after mapping.
     */
    @Mapping(target = "totalOrders",  ignore = true)
    @Mapping(target = "totalBookings", ignore = true)
    public abstract CustomerSummaryResponse toSummaryResponse(Customer customer);

    // ── Customer → CustomerResponse (full detail) ─────────────────────────

    /**
     * Full detail mapping. Financial aggregates are set by the service layer.
     */
    @Mapping(target = "totalOrders",        ignore = true)
    @Mapping(target = "totalInvoiceAmount", ignore = true)
    @Mapping(target = "totalPaidAmount",    ignore = true)
    @Mapping(target = "outstandingDues",    ignore = true)
    public abstract CustomerResponse toDetailResponse(Customer customer);

    // ── Customer → CustomerProfileResponse ────────────────────────────────

    /**
     * Rich profile mapping. List summaries and aggregates are set by the service.
     */
    @Mapping(target = "recentOrders",       ignore = true)
    @Mapping(target = "recentInvoices",     ignore = true)
    @Mapping(target = "totalOrders",        ignore = true)
    @Mapping(target = "totalInvoices",      ignore = true)
    @Mapping(target = "totalBusinessValue", ignore = true)
    @Mapping(target = "totalPaidAmount",    ignore = true)
    @Mapping(target = "outstandingDues",    ignore = true)
    public abstract CustomerProfileResponse toProfileResponse(Customer customer);

    // ── Order → OrderSummaryDto ───────────────────────────────────────────

    public abstract OrderSummaryDto toOrderSummary(Order order);

    // ── Invoice → InvoiceSummaryDto ───────────────────────────────────────

    public abstract InvoiceSummaryDto toInvoiceSummary(Invoice invoice);

    // ── Patch update: apply non-null fields from request onto entity ───────

    /**
     * Applies only non-null fields from {@code request} onto the existing
     * {@code customer} entity. MapStruct respects
     * {@code NullValuePropertyMappingStrategy.IGNORE} at the mapper level.
     *
     * <p>Fields managed separately (isRegular, regularSince, dateJoined) are
     * excluded to avoid accidental overwrites.
     */
    @Mapping(target = "id",           ignore = true)
    @Mapping(target = "isRegular",    ignore = true)
    @Mapping(target = "regularSince", ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "updatedAt",    ignore = true)
    @Mapping(target = "deleted",      ignore = true)
    @Mapping(target = "createdBy",    ignore = true)
    @Mapping(target = "orders",       ignore = true)
    @Mapping(target = "invoices",     ignore = true)
    @Mapping(target = "payments",     ignore = true)
    @Mapping(target = "visits",       ignore = true)
    @Mapping(target = "reminders",    ignore = true)
    public abstract void applyUpdate(
            com.erp.system.dto.request.UpdateCustomerRequest request,
            @MappingTarget Customer customer
    );
}
