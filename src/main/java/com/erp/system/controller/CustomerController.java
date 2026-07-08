package com.erp.system.controller;

import com.erp.system.dto.request.CreateCustomerRequest;
import com.erp.system.dto.request.CustomerFilterRequest;
import com.erp.system.dto.request.RegularCustomerConfigRequest;
import com.erp.system.dto.request.UpdateCustomerRequest;
import com.erp.system.dto.response.*;
import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import com.erp.system.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Customer Management REST Controller.
 *
 * <p>Base path: {@code /api/customers}
 *
 * <h3>Endpoints</h3>
 * <pre>
 *  POST   /api/customers                        – Create customer
 *  PATCH  /api/customers/{id}                   – Update customer (patch)
 *  DELETE /api/customers/{id}                   – Soft-delete customer
 *  GET    /api/customers/{id}                   – Get customer by ID
 *  GET    /api/customers                        – List all (paginated + filters)
 *  GET    /api/customers/search                 – Search & filter via Spec/criteria
 *  GET    /api/customers/{id}/profile           – Full customer profile
 *  GET    /api/customers/by-area                – Area-wise grouped list with counts
 *  GET    /api/customers/by-booking-count       – Regular listing by booking count
 *  GET    /api/customers/pending-payments       – Outstanding dues view
 *  GET    /api/customers/{id}/history           – Customer timeline history
 *  POST   /api/customers/regular/recalculate    – On-demand regular-customer recalc
 * </pre>
 */
@Slf4j
@RestController
@RequestMapping("/api/customers")
@CrossOrigin
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // ── POST /api/customers ───────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CreateCustomerRequest request) {

        CustomerResponse data = customerService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully.", data));
    }

    // ── PATCH /api/customers/{id} ─────────────────────────────────────────

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request) {

        CustomerResponse data = customerService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully.", data));
    }

    // ── DELETE /api/customers/{id} ────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully."));
    }

    // ── GET /api/customers/{id} ───────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Customer retrieved successfully.", customerService.getById(id))
        );
    }

    // ── GET /api/customers ────────────────────────────────────────────────
    /**
     * List all customers with optional filters.
     *
     * @param search        Keyword search (name / mobile / area)
     * @param customerType  Filter by type (RETAIL, WHOLESALE, DEALER, CORPORATE, OTHER)
     * @param customerStatus Filter by status (ACTIVE, INACTIVE, BLOCKED)
     * @param isActive      Filter by active flag
     * @param isRegular     Filter by regular flag
     * @param area          Filter by area (exact, case-insensitive)
     * @param city          Filter by city (exact, case-insensitive)
     * @param dateJoinedFrom Lower bound for dateJoined (yyyy-MM-dd)
     * @param dateJoinedTo   Upper bound for dateJoined (yyyy-MM-dd)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CustomerSummaryResponse>>> listAll(
            @RequestParam(required = false)                                  String         search,
            @RequestParam(required = false)                                  CustomerType   customerType,
            @RequestParam(required = false)                                  CustomerStatus customerStatus,
            @RequestParam(required = false)                                  Boolean        isActive,
            @RequestParam(required = false)                                  Boolean        isRegular,
            @RequestParam(required = false)                                  String         area,
            @RequestParam(required = false)                                  String         city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateJoinedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateJoinedTo,
            @RequestParam(defaultValue = "0")           int    page,
            @RequestParam(defaultValue = "20")          int    size,
            @RequestParam(defaultValue = "dateJoined")  String sortBy,
            @RequestParam(defaultValue = "desc")        String sortDir) {

        CustomerFilterRequest filter = buildFilter(search, customerType, customerStatus,
                isActive, isRegular, area, city, dateJoinedFrom, dateJoinedTo);

        PagedResponse<CustomerSummaryResponse> data =
                customerService.listAll(filter, buildPageable(page, size, sortBy, sortDir));

        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully.", data));
    }

    // ── GET /api/customers/search ─────────────────────────────────────────
    /**
     * Spec/criteria-backed search endpoint. Accepts the same filter params as
     * list-all but uses JPA Specifications under the hood for cleaner query building.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerSummaryResponse>>> search(
            @RequestParam(required = false)                                  String         search,
            @RequestParam(required = false)                                  CustomerType   customerType,
            @RequestParam(required = false)                                  CustomerStatus customerStatus,
            @RequestParam(required = false)                                  Boolean        isActive,
            @RequestParam(required = false)                                  Boolean        isRegular,
            @RequestParam(required = false)                                  String         area,
            @RequestParam(required = false)                                  String         city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateJoinedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateJoinedTo,
            @RequestParam(defaultValue = "0")           int    page,
            @RequestParam(defaultValue = "20")          int    size,
            @RequestParam(defaultValue = "name")        String sortBy,
            @RequestParam(defaultValue = "asc")         String sortDir) {

        CustomerFilterRequest filter = buildFilter(search, customerType, customerStatus,
                isActive, isRegular, area, city, dateJoinedFrom, dateJoinedTo);

        PagedResponse<CustomerSummaryResponse> data =
                customerService.searchAndFilter(filter, buildPageable(page, size, sortBy, sortDir));

        return ResponseEntity.ok(ApiResponse.success("Search results retrieved.", data));
    }

    // ── GET /api/customers/{id}/profile ──────────────────────────────────

    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Customer profile retrieved successfully.",
                        customerService.getProfile(id))
        );
    }

    // ── GET /api/customers/by-area ────────────────────────────────────────

    @GetMapping("/by-area")
    public ResponseEntity<ApiResponse<PagedResponse<AreaCustomerSummary>>> listByArea(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0")              int    page,
            @RequestParam(defaultValue = "50")             int    size,
            @RequestParam(defaultValue = "totalCustomers") String sortBy,
            @RequestParam(defaultValue = "desc")           String sortDir) {

        PagedResponse<AreaCustomerSummary> data =
                customerService.listByArea(city, buildPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(ApiResponse.success("Area summary retrieved successfully.", data));
    }

    // ── GET /api/customers/by-booking-count ──────────────────────────────

    @GetMapping("/by-booking-count")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerSummaryResponse>>> listByBookingCount(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<CustomerSummaryResponse> data =
                customerService.listByBookingCount(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Customers by booking count retrieved.", data));
    }

    // ── GET /api/customers/pending-payments ───────────────────────────────

    /**
     * @param sortBy   totalDueAmount | maxOverdueDays (default: totalDueAmount)
     * @param sortDir  asc | desc (default: desc)
     */
    @GetMapping("/pending-payments")
    public ResponseEntity<ApiResponse<PagedResponse<PendingPaymentResponse>>> listPendingPayments(
            @RequestParam(defaultValue = "0")              int    page,
            @RequestParam(defaultValue = "20")             int    size,
            @RequestParam(defaultValue = "totalDueAmount") String sortBy,
            @RequestParam(defaultValue = "desc")           String sortDir) {

        PagedResponse<PendingPaymentResponse> data =
                customerService.listPendingPayments(buildPageable(page, size, sortBy, sortDir));
        return ResponseEntity.ok(ApiResponse.success("Pending payments retrieved successfully.", data));
    }

    // ── GET /api/customers/{id}/history ───────────────────────────────────

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerHistoryEntry>>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<CustomerHistoryEntry> data =
                customerService.getHistory(id, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success("Customer history retrieved successfully.", data));
    }

    // ── POST /api/customers/regular/recalculate ───────────────────────────

    /**
     * On-demand trigger for the regular-customer algorithm.
     * The request body is optional — omit it to use application.properties defaults.
     */
    @PostMapping("/regular/recalculate")
    public ResponseEntity<ApiResponse<RegularCustomerRecalcResult>> recalculateRegular(
            @Valid @RequestBody(required = false) RegularCustomerConfigRequest config) {

        RegularCustomerRecalcResult result = customerService.recalculateRegularCustomers(config);
        return ResponseEntity.ok(ApiResponse.success(
                "Regular customer recalculation completed successfully.", result));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private CustomerFilterRequest buildFilter(String search, CustomerType customerType,
                                              CustomerStatus customerStatus, Boolean isActive,
                                              Boolean isRegular, String area, String city,
                                              LocalDate dateJoinedFrom, LocalDate dateJoinedTo) {
        return CustomerFilterRequest.builder()
                .search(search)
                .customerType(customerType)
                .customerStatus(customerStatus)
                .isActive(isActive)
                .isRegular(isRegular)
                .area(area)
                .city(city)
                .dateJoinedFrom(dateJoinedFrom)
                .dateJoinedTo(dateJoinedTo)
                .build();
    }

    private org.springframework.data.domain.Pageable buildPageable(int page, int size,
                                                                    String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
