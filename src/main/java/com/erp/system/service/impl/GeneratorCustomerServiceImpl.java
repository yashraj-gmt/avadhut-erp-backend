package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateCustomerRequest;
import com.erp.system.dto.request.CustomerFilterRequest;
import com.erp.system.dto.request.RegularCustomerConfigRequest;
import com.erp.system.dto.request.UpdateCustomerRequest;
import com.erp.system.dto.response.*;
import com.erp.system.entity.Customer;
import com.erp.system.entity.Invoice;
import com.erp.system.entity.Order;
import com.erp.system.entity.Payment;
import com.erp.system.exception.AppException;
import com.erp.system.exception.ResourceNotFoundException;
import com.erp.system.mapper.CustomerMapper;
import com.erp.system.repository.*;
import com.erp.system.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeneratorCustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository    orderRepository;
    private final InvoiceRepository  invoiceRepository;
    private final PaymentRepository  paymentRepository;
    private final CustomerMapper     customerMapper;

    // ── Regular-customer config from application.properties ───────────────

    @Value("${erp.customer.regular.min-orders:3}")
    private int defaultMinOrders;

    @Value("${erp.customer.regular.lookback-months:6}")
    private int defaultLookbackMonths;

    @Value("${erp.customer.regular.min-total-spend:10000}")
    private BigDecimal defaultMinTotalSpend;

    // ── CRUD ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {

        if (customerRepository.existsByMobile(request.getMobile())) {
            throw new AppException(
                    "A customer with mobile '" + request.getMobile() + "' already exists.",
                    HttpStatus.CONFLICT
            );
        }

        Customer customer = new Customer();
        applyCreateFields(customer, request);
        Customer saved = customerRepository.save(customer);

        log.info("Customer created: id={}, name={}", saved.getId(), saved.getName());
        return enrichDetailResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest request) {

        Customer customer = findOrThrow(id);

        if (request.getMobile() != null
                && !request.getMobile().equals(customer.getMobile())
                && customerRepository.existsByMobileAndIdNot(request.getMobile(), id)) {
            throw new AppException(
                    "Mobile '" + request.getMobile() + "' is already registered to another customer.",
                    HttpStatus.CONFLICT
            );
        }

        customerMapper.applyUpdate(request, customer);
        Customer saved = customerRepository.save(customer);

        log.info("Customer updated: id={}", saved.getId());
        return enrichDetailResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Customer customer = findOrThrow(id);
        customer.setDeleted(true);
        customer.setIsActive(false);
        customerRepository.save(customer);
        log.info("Customer soft-deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return enrichDetailResponse(findOrThrow(id));
    }

    // ── List / Search ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerSummaryResponse> listAll(CustomerFilterRequest filter,
                                                          Pageable pageable) {
        // Build spec and delegate to the same Spec-based path for consistency
        Specification<Customer> spec = CustomerSpecification.from(filter);
        Page<Customer> page = customerRepository.findAll(spec, pageable);
        return PagedResponse.from(page.map(this::toSummaryWithCounts));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerSummaryResponse> searchAndFilter(CustomerFilterRequest filter,
                                                                  Pageable pageable) {
        Specification<Customer> spec = CustomerSpecification.from(filter);
        Page<Customer> page = customerRepository.findAll(spec, pageable);
        return PagedResponse.from(page.map(this::toSummaryWithCounts));
    }

    // ── Rich Profile ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long id) {

        Customer customer = findOrThrow(id);

        // Fetch recent orders/invoices (limit 10 each for the summary)
        List<Order>   recentOrders   = orderRepository.findTopByCustomerId(id, 10);
        List<Invoice> recentInvoices = invoiceRepository.findTopByCustomerId(id, 10);

        BigDecimal totalInvoiceAmount = invoiceRepository.sumFinalAmountByCustomerId(id);
        BigDecimal totalPaidAmount    = invoiceRepository.sumPaidAmountByCustomerId(id);
        BigDecimal outstandingDues    = totalInvoiceAmount.subtract(totalPaidAmount);

        long totalOrders   = orderRepository.countByCustomerIdAndDeletedFalse(id);
        long totalInvoices = recentInvoices.size(); // approximate; replace with count query if needed

        CustomerProfileResponse profile = customerMapper.toProfileResponse(customer);
        profile.setRecentOrders(recentOrders.stream().map(customerMapper::toOrderSummary).toList());
        profile.setRecentInvoices(recentInvoices.stream().map(customerMapper::toInvoiceSummary).toList());
        profile.setTotalOrders(totalOrders);
        profile.setTotalInvoices(totalInvoices);
        profile.setTotalBusinessValue(totalInvoiceAmount);
        profile.setTotalPaidAmount(totalPaidAmount);
        profile.setOutstandingDues(outstandingDues.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO : outstandingDues);

        return profile;
    }

    // ── Area-wise ─────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AreaCustomerSummary> listByArea(String city, Pageable pageable) {

        Page<Object[]> rows = customerRepository.findAreaSummary(city, pageable);

        Page<AreaCustomerSummary> result = rows.map(row -> AreaCustomerSummary.builder()
                .area(             (String) row[0])
                .city(             (String) row[1])
                .totalCustomers(   toLong(row[2]))
                .activeCustomers(  toLong(row[3]))
                .regularCustomers( toLong(row[4]))
                .build()
        );

        return PagedResponse.from(result);
    }

    // ── Regular listing by booking count ─────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerSummaryResponse> listByBookingCount(Pageable pageable) {
        Page<Customer> page = customerRepository.findAllOrderedByBookingCountDesc(pageable);
        return PagedResponse.from(page.map(this::toSummaryWithCounts));
    }

    // ── Pending Payments ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PendingPaymentResponse> listPendingPayments(Pageable pageable) {

        // 1. Get all customer IDs with pending invoices
        List<Long> customerIds = customerRepository.findCustomerIdsWithPendingPayments();
        if (customerIds.isEmpty()) {
            return PagedResponse.from(Page.empty(pageable));
        }

        // 2. Fetch all their unpaid invoices in one query
        List<Invoice> unpaidInvoices = invoiceRepository.findUnpaidByCustomerIds(customerIds);

        // 3. Fetch last payment dates in bulk
        Map<Long, LocalDate> lastPaymentDates = buildLastPaymentMap(customerIds);

        // 4. Fetch customer entities for basic info
        List<Customer> customers = customerRepository.findAllById(customerIds);
        Map<Long, Customer> customerMap = customers.stream()
                .collect(Collectors.toMap(Customer::getId, c -> c));

        // 5. Group invoices by customer and build response rows
        LocalDate today = LocalDate.now();
        Map<Long, List<Invoice>> invoicesByCustomer = unpaidInvoices.stream()
                .collect(Collectors.groupingBy(i -> i.getCustomer().getId()));

        List<PendingPaymentResponse> rows = customerIds.stream()
                .filter(cid -> customerMap.containsKey(cid) && invoicesByCustomer.containsKey(cid))
                .map(cid -> {
                    Customer c = customerMap.get(cid);
                    List<Invoice> invoices = invoicesByCustomer.get(cid);

                    List<OverdueInvoiceInfo> overdueInfo = invoices.stream().map(inv -> {
                        long overdueDays = inv.getDueDate() != null
                                ? Math.max(0, today.toEpochDay() - inv.getDueDate().toEpochDay())
                                : 0L;
                        return OverdueInvoiceInfo.builder()
                                .invoiceId(inv.getId())
                                .invoiceNumber(inv.getInvoiceNumber())
                                .pendingAmount(inv.getPendingAmount())
                                .dueDate(inv.getDueDate())
                                .overdueDays(overdueDays)
                                .build();
                    }).toList();

                    BigDecimal totalDue = invoices.stream()
                            .map(Invoice::getPendingAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    long maxOverdueDays = overdueInfo.stream()
                            .mapToLong(OverdueInvoiceInfo::getOverdueDays)
                            .max().orElse(0L);

                    return PendingPaymentResponse.builder()
                            .customerId(c.getId())
                            .customerName(c.getName())
                            .mobile(c.getMobile())
                            .area(c.getArea())
                            .city(c.getCity())
                            .totalDueAmount(totalDue)
                            .lastPaymentDate(lastPaymentDates.get(cid))
                            .maxOverdueDays(maxOverdueDays)
                            .overdueInvoices(overdueInfo)
                            .build();
                })
                .collect(Collectors.toList());

        // 6. Apply sort from Pageable (totalDueAmount or maxOverdueDays)
        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order sortOrder = sort.iterator().next();
            Comparator<PendingPaymentResponse> comparator =
                    resolveComparator(sortOrder.getProperty());
            if (sortOrder.isDescending()) comparator = comparator.reversed();
            rows.sort(comparator);
        }

        // 7. Manual pagination of in-memory list
        return toPagedResponse(rows, pageable);
    }

    // ── History Timeline ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerHistoryEntry> getHistory(Long customerId, Pageable pageable) {

        findOrThrow(customerId); // 404 guard

        List<Order>   orders   = orderRepository.findHistoryByCustomerId(customerId);
        List<Invoice> invoices = invoiceRepository.findHistoryByCustomerId(customerId);
        List<Payment> payments = paymentRepository.findHistoryByCustomerId(customerId);

        // Convert each entity type to a uniform CustomerHistoryEntry
        List<CustomerHistoryEntry> timeline = new ArrayList<>();

        orders.forEach(o -> timeline.add(CustomerHistoryEntry.builder()
                .type("ORDER")
                .referenceId(o.getId())
                .referenceNumber(o.getOrderNumber())
                .amount(o.getFinalAmount())
                .status(o.getOrderStatus().name())
                .occurredAt(o.getCreatedAt())
                .build()));

        invoices.forEach(i -> timeline.add(CustomerHistoryEntry.builder()
                .type("INVOICE")
                .referenceId(i.getId())
                .referenceNumber(i.getInvoiceNumber())
                .amount(i.getFinalAmount())
                .status(i.getPaymentStatus().name())
                .occurredAt(i.getCreatedAt())
                .build()));

        payments.forEach(p -> timeline.add(CustomerHistoryEntry.builder()
                .type("PAYMENT")
                .referenceId(p.getId())
                .referenceNumber("PMT-" + p.getId())
                .amount(p.getAmount())
                .status(p.getPaymentMode().name())
                .occurredAt(p.getCreatedAt())
                .build()));

        // Sort newest first
        timeline.sort(Comparator.comparing(CustomerHistoryEntry::getOccurredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return toPagedResponse(timeline, pageable);
    }

    // ── Regular Customer Algorithm ────────────────────────────────────────

    /**
     * Scheduled nightly recalculation — runs at 2 AM daily.
     * Uses application.properties defaults.
     */
    @Scheduled(cron = "${erp.customer.regular.cron:0 0 2 * * *}")
    public void scheduledRegularRecalc() {
        log.info("Regular-customer scheduled recalculation started.");
        RegularCustomerRecalcResult result = recalculateRegularCustomers(null);
        log.info("Regular-customer recalculation done: processed={}, flagged={}, unflagged={}",
                result.getTotalProcessed(), result.getTotalFlagged(), result.getTotalUnflagged());
    }

    @Override
    @Transactional
    public RegularCustomerRecalcResult recalculateRegularCustomers(RegularCustomerConfigRequest config) {

        // Resolve config: request body overrides defaults
        int        minOrders       = (config != null && config.getMinOrders()       != null)
                ? config.getMinOrders()       : defaultMinOrders;
        int        lookbackMonths  = (config != null && config.getLookbackMonths()  != null)
                ? config.getLookbackMonths()  : defaultLookbackMonths;
        BigDecimal minTotalSpend   = (config != null && config.getMinTotalSpend()   != null)
                ? config.getMinTotalSpend()   : defaultMinTotalSpend;

        LocalDateTime since = LocalDateTime.now().minusMonths(lookbackMonths);
        List<Customer> customers = customerRepository.findAllActiveForRecalc();

        int totalProcessed = 0;
        int totalFlagged   = 0;
        int totalUnflagged = 0;

        for (Customer customer : customers) {
            totalProcessed++;
            Long   cid          = customer.getId();
            long   recentOrders = orderRepository.countByCustomerIdSince(cid, since);
            BigDecimal totalSpend = orderRepository.sumFinalAmountByCustomerId(cid);

            // Qualifies if EITHER: order count threshold OR spend threshold is met
            boolean qualifies = recentOrders >= minOrders
                    || totalSpend.compareTo(minTotalSpend) >= 0;

            boolean wasRegular = Boolean.TRUE.equals(customer.getIsRegular());

            if (qualifies && !wasRegular) {
                customer.setIsRegular(true);
                customer.setRegularSince(LocalDate.now());
                totalFlagged++;
            } else if (!qualifies && wasRegular) {
                customer.setIsRegular(false);
                customer.setRegularSince(null);
                totalUnflagged++;
            }
        }

        customerRepository.saveAll(customers);

        return RegularCustomerRecalcResult.builder()
                .totalProcessed(totalProcessed)
                .totalFlagged(totalFlagged)
                .totalUnflagged(totalUnflagged)
                .minOrdersUsed(minOrders)
                .lookbackMonthsUsed(lookbackMonths)
                .minTotalSpendUsed(minTotalSpend)
                .recalculatedAt(LocalDateTime.now())
                .build();
    }

    // ── Internal Helpers ──────────────────────────────────────────────────

    private Customer findOrThrow(Long id) {
        return customerRepository.findById(id)
                .filter(c -> !Boolean.TRUE.equals(c.getDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    private void applyCreateFields(Customer customer, CreateCustomerRequest req) {
        customer.setName(req.getName().trim());
        customer.setMobile(req.getMobile().trim());
        customer.setAlternateMobile(req.getAlternateMobile());
        customer.setEmail(req.getEmail());
        customer.setAddress(req.getAddress());
        customer.setCity(req.getCity());
        customer.setArea(req.getArea());
        customer.setPincode(req.getPincode());
        customer.setCustomerType(req.getCustomerType());
        customer.setNotes(req.getNotes());
        if (req.getDateJoined() != null) {
            customer.setDateJoined(req.getDateJoined());
        }
    }

    /** Maps Customer → CustomerResponse and enriches with computed aggregates. */
    private CustomerResponse enrichDetailResponse(Customer customer) {
        Long id = customer.getId();
        CustomerResponse response = customerMapper.toDetailResponse(customer);

        BigDecimal totalInvoiceAmt = invoiceRepository.sumFinalAmountByCustomerId(id);
        BigDecimal totalPaidAmt    = invoiceRepository.sumPaidAmountByCustomerId(id);
        BigDecimal outstanding     = totalInvoiceAmt.subtract(totalPaidAmt);

        response.setTotalOrders(orderRepository.countByCustomerIdAndDeletedFalse(id));
        response.setTotalInvoiceAmount(totalInvoiceAmt);
        response.setTotalPaidAmount(totalPaidAmt);
        response.setOutstandingDues(outstanding.compareTo(BigDecimal.ZERO) < 0
                ? BigDecimal.ZERO : outstanding);
        return response;
    }

    /** Maps Customer → CustomerSummaryResponse and enriches with order counts. */
    private CustomerSummaryResponse toSummaryWithCounts(Customer customer) {
        CustomerSummaryResponse summary = customerMapper.toSummaryResponse(customer);
        long total = orderRepository.countByCustomerIdAndDeletedFalse(customer.getId());
        summary.setTotalOrders(total);
        summary.setTotalBookings(total); // bookings = orders in this domain
        return summary;
    }

    /** Build a Map<customerId, lastPaymentDate> from bulk query result. */
    private Map<Long, LocalDate> buildLastPaymentMap(List<Long> customerIds) {
        List<Object[]> rows = paymentRepository.findLastPaymentDatesByCustomerIds(customerIds);
        Map<Long, LocalDate> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(toLong(row[0]), (LocalDate) row[1]);
        }
        return map;
    }

    /** Resolve sort comparator for PendingPaymentResponse. */
    private Comparator<PendingPaymentResponse> resolveComparator(String property) {
        return switch (property) {
            case "maxOverdueDays"  -> Comparator.comparingLong(r ->
                    r.getMaxOverdueDays() != null ? r.getMaxOverdueDays() : 0L);
            default                -> Comparator.comparing(r ->
                    r.getTotalDueAmount() != null ? r.getTotalDueAmount() : BigDecimal.ZERO);
        };
    }

    /** Manually paginate an in-memory list. */
    private <T> PagedResponse<T> toPagedResponse(List<T> all, Pageable pageable) {
        int total = all.size();
        int start = (int) Math.min(pageable.getOffset(), total);
        int end   = Math.min(start + pageable.getPageSize(), total);
        List<T> slice = all.subList(start, end);
        Page<T> page  = new PageImpl<>(slice, pageable, total);
        return PagedResponse.from(page);
    }

    /** Safely cast a Number to Long (handles Long, BigInteger, etc.). */
    private Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long l) return l;
        if (o instanceof Number n) return n.longValue();
        return 0L;
    }
}
