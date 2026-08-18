package com.erp.system.service.impl;

import com.erp.system.dto.request.CreateOrderRequest;
import com.erp.system.dto.request.UpdateOrderRequest;
import com.erp.system.dto.request.UpdateOrderBillingRequest;
import com.erp.system.dto.response.OrderResponse;
import com.erp.system.dto.response.PagedResponse;
import com.erp.system.entity.Customer;
import com.erp.system.entity.Generator;
import com.erp.system.entity.Order;
import com.erp.system.entity.OrderItem;
import com.erp.system.entity.OrderItemDieselEntry;
import com.erp.system.enums.OrderStatus;
import com.erp.system.enums.BillingStatus;
import com.erp.system.enums.PaymentStatus;
import com.erp.system.exception.AppException;
import com.erp.system.repository.CustomerRepository;
import com.erp.system.repository.GeneratorRepository;
import com.erp.system.repository.OrderRepository;
import com.erp.system.service.OrderService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final GeneratorRepository generatorRepository;

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        log.info("Creating order for client: {}", request.getClientName());

        Customer customer = customerRepository.findFirstByNameAndMobile(request.getClientName(), request.getContactNumber())
                .orElseGet(() -> {
                    Customer newCust = new Customer();
                    newCust.setName(request.getClientName());
                    newCust.setMobile(request.getContactNumber());
                    return customerRepository.save(newCust);
                });

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(customer);
        
        mapRequestToEntity(request, order);
        
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAll(String search, String status, Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("deleted")));

            if (search != null && !search.trim().isEmpty()) {
                String searchLower = "%" + search.toLowerCase() + "%";
                Predicate orderNum = cb.like(cb.lower(root.get("orderNumber")), searchLower);
                Predicate custName = cb.like(cb.lower(root.join("customer").get("name")), searchLower);
                Predicate custMobile = cb.like(cb.lower(root.join("customer").get("mobile")), searchLower);
                predicates.add(cb.or(orderNum, custName, custMobile));
            }

            if (status != null && !status.trim().isEmpty()) {
                String st = status.trim().toUpperCase();
                if (st.equals("BOOKED")) st = "PENDING";
                try {
                    com.erp.system.enums.OrderStatus os = com.erp.system.enums.OrderStatus.valueOf(st);
                    predicates.add(cb.equal(root.get("orderStatus"), os));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid order status filter: {}", status);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Order> page = orderRepository.findAll(spec, pageable);
        List<OrderResponse> dtos = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PagedResponse.<OrderResponse>builder()
                .content(dtos)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = findOrderOrThrow(id);
        return toDto(order);
    }

    @Override
    @Transactional
    public OrderResponse update(Long id, UpdateOrderRequest request) {
        Order order = findOrderOrThrow(id);
        
        // Handle customer assignment or update
        String newMobile = request.getContactNumber();
        String newName = request.getClientName();
        Customer currentCustomer = order.getCustomer();

        if (currentCustomer != null) {
            currentCustomer.setName(newName);
            currentCustomer.setMobile(newMobile);
            customerRepository.save(currentCustomer);
        } else {
            // Fallback if no customer was set
            Customer customer = customerRepository.findFirstByNameAndMobile(newName, newMobile)
                .orElseGet(() -> {
                    Customer newCust = new Customer();
                    newCust.setName(newName);
                    newCust.setMobile(newMobile);
                    return customerRepository.save(newCust);
                });
            order.setCustomer(customer);
        }

        // clear old items
        order.getOrderItems().clear();
        
        // map request
        CreateOrderRequest tempRequest = new CreateOrderRequest();
        tempRequest.setAlternateMobile(request.getAlternateMobile());
        tempRequest.setOperatorName(request.getOperatorName());
        tempRequest.setOperatorMobile(request.getOperatorMobile());
        tempRequest.setCableRequired(request.getCableRequired());
        tempRequest.setDieselType(request.getDieselType());
        tempRequest.setSiteAddress(request.getSiteAddress());
        tempRequest.setSiteAddressLink(request.getSiteAddressLink());
        tempRequest.setRemarks(request.getRemarks());
        tempRequest.setFunctionDateFrom(request.getFunctionDateFrom());
        tempRequest.setFunctionDateTo(request.getFunctionDateTo());
        tempRequest.setFunctionDate(request.getFunctionDate());
        tempRequest.setGenerators(request.getGenerators());

        mapRequestToEntity(tempRequest, order);
        
        order = orderRepository.save(order);
        return toDto(order);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Order order = findOrderOrThrow(id);
        order.setDeleted(true);
        orderRepository.save(order);
    }

    private Order findOrderOrThrow(Long id) {
        return orderRepository.findById(id)
                .filter(o -> !o.getDeleted())
                .orElseThrow(() -> new AppException("Order not found", HttpStatus.NOT_FOUND));
    }

    private String generateOrderNumber() {
        // e.g., GO20261
        String prefix = "GO" + Year.now().getValue();
        Long maxId = orderRepository.getMaxId();
        long count = (maxId == null ? 0 : maxId) + 1;
        return prefix + count;
    }

    private void mapRequestToEntity(CreateOrderRequest request, Order order) {
        order.setAlternateMobile(request.getAlternateMobile());
        order.setOperatorName(request.getOperatorName());
        order.setOperatorMobile(request.getOperatorMobile());
        order.setCableRequired(request.getCableRequired());
        order.setWithDiesel(request.getDieselType() != null && request.getDieselType().equals("WITH_OWNER"));
        order.setSiteAddress(request.getSiteAddress());
        order.setSiteAddressLink(request.getSiteAddressLink());
        order.setNotes(request.getRemarks());

        if (request.getFunctionDateFrom() != null) {
            order.setFunctionDateFrom(request.getFunctionDateFrom());
            order.setFunctionDateTo(request.getFunctionDateTo());
        } else if (request.getFunctionDate() != null) {
            // "2026-07-01 to 2026-07-03" -> split it
            String[] parts = request.getFunctionDate().split(" to ");
            if (parts.length > 0) order.setFunctionDateFrom(LocalDate.parse(parts[0].trim()));
            if (parts.length > 1) order.setFunctionDateTo(LocalDate.parse(parts[1].trim()));
            else if (parts.length == 1) order.setFunctionDateTo(LocalDate.parse(parts[0].trim()));
        }

        // Validate stock availability for each requested generator
        LocalDate startDate = order.getFunctionDateFrom();
        LocalDate endDate = order.getFunctionDateTo();
        if (startDate != null && endDate != null && request.getGenerators() != null) {
            Long excludeOrderId = order.getId();
            List<OrderItem> overlappingBookings = generatorRepository.findAllOverlappingBookings(startDate, endDate, excludeOrderId);

            java.util.Map<Long, List<OrderItem>> bookingsByGenerator = overlappingBookings.stream()
                    .filter(oi -> oi.getGenerator() != null)
                    .collect(Collectors.groupingBy(oi -> oi.getGenerator().getId()));

            java.util.Map<Long, Integer> requestedCounts = new java.util.HashMap<>();
            for (CreateOrderRequest.OrderItemRequest itemReq : request.getGenerators()) {
                Generator generator;
                String genIdStr = itemReq.getGeneratorId();
                try {
                    Long genIdLong = Long.parseLong(genIdStr);
                    generator = generatorRepository.findById(genIdLong)
                            .orElseThrow(() -> new AppException("Generator not found with id: " + genIdStr, HttpStatus.NOT_FOUND));
                } catch (NumberFormatException e) {
                    generator = generatorRepository.findByGeneratorCodeIgnoreCase(genIdStr)
                            .orElseThrow(() -> new AppException("Generator not found with code: " + genIdStr, HttpStatus.NOT_FOUND));
                }
                requestedCounts.put(generator.getId(), requestedCounts.getOrDefault(generator.getId(), 0) + 1);
            }

            for (java.util.Map.Entry<Long, Integer> entry : requestedCounts.entrySet()) {
                Long generatorId = entry.getKey();
                int requestedQty = entry.getValue();

                Generator generator = generatorRepository.findById(generatorId)
                        .orElseThrow(() -> new AppException("Generator not found", HttpStatus.NOT_FOUND));

                int totalStock = generator.getStockQuantity() != null ? generator.getStockQuantity() : 0;
                List<OrderItem> bookings = bookingsByGenerator.getOrDefault(generatorId, java.util.Collections.emptyList());

                int minAvailable = totalStock;
                for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
                    final LocalDate currentDay = d;
                    int bookedOnDay = bookings.stream()
                            .filter(oi -> {
                                Order o = oi.getOrder();
                                return o != null 
                                    && o.getFunctionDateFrom() != null 
                                    && o.getFunctionDateTo() != null
                                    && !o.getFunctionDateFrom().isAfter(currentDay) 
                                    && !o.getFunctionDateTo().isBefore(currentDay);
                            })
                            .mapToInt(oi -> oi.getQuantity() != null ? oi.getQuantity() : 0)
                            .sum();

                    int availableOnDay = totalStock - bookedOnDay;
                    if (availableOnDay < minAvailable) {
                        minAvailable = availableOnDay;
                    }
                }

                if (minAvailable < 0) {
                    minAvailable = 0;
                }

                if (requestedQty > minAvailable) {
                    throw new AppException(
                        "Insufficient stock for generator '" + generator.getName() + 
                        "' for the selected dates. Requested: " + requestedQty + 
                        ", Available: " + minAvailable, 
                        HttpStatus.BAD_REQUEST
                    );
                }
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        if (request.getGenerators() != null) {
            for (CreateOrderRequest.OrderItemRequest itemReq : request.getGenerators()) {
                // Resolve generator by numeric ID or by generator code (e.g. "GEN-012")
                Generator generator;
                String genIdStr = itemReq.getGeneratorId();
                try {
                    Long genIdLong = Long.parseLong(genIdStr);
                    generator = generatorRepository.findById(genIdLong)
                            .orElseThrow(() -> new AppException("Generator not found with id: " + genIdStr, HttpStatus.NOT_FOUND));
                } catch (NumberFormatException e) {
                    // Not a numeric ID — try looking up by generator code
                    generator = generatorRepository.findByGeneratorCodeIgnoreCase(genIdStr)
                            .orElseThrow(() -> new AppException("Generator not found with code: " + genIdStr, HttpStatus.NOT_FOUND));
                }

                OrderItem item = new OrderItem();
                item.setGenerator(generator);
                item.setProductName(generator.getName());
                item.setQuantity(1);
                
                // Generator rent is ALWAYS partyDieselRentPrice (base generator rent)
                BigDecimal rate = itemReq.getRate() != null ? itemReq.getRate() : generator.getPartyDieselRentPrice();
                if(rate == null) rate = BigDecimal.ZERO;
                item.setRate(rate);

                // Diesel rate is withDieselRentPrice if WITH_OWNER, else 0
                if (order.getWithDiesel()) {
                    item.setDieselRate(generator.getWithDieselRentPrice() != null ? generator.getWithDieselRentPrice() : BigDecimal.ZERO);
                } else {
                    item.setDieselRate(BigDecimal.ZERO);
                }

                item.setCableSize(itemReq.getCableSize());
                if (itemReq.getCableRate() != null) {
                    item.setCableRate(itemReq.getCableRate());
                }
                item.setStartTime(itemReq.getStartTime());
                item.setEndTime(itemReq.getEndTime());
                item.setDuration(itemReq.getDuration());

                // Default simple calculation. Note: In real app duration/days would multiply rate.
                // Assuming rate is per day. UI will calculate and send final logic if needed, but backend handles simple math.
                // Assuming it's a fixed rate from generator entity for now unless overridden
                item.setTotalAmount(rate);
                subtotal = subtotal.add(item.getTotalAmount());
                order.addItem(item);
            }
        }
        order.setSubtotal(subtotal);
        order.setFinalAmount(subtotal);
    }

    private OrderResponse toDto(Order o) {
        String funcDate = "";
        if(o.getFunctionDateFrom() != null) {
            funcDate = o.getFunctionDateFrom().toString();
            if(o.getFunctionDateTo() != null && !o.getFunctionDateFrom().equals(o.getFunctionDateTo())) {
                funcDate += " to " + o.getFunctionDateTo().toString();
            }
        }

        List<OrderResponse.OrderItemResponse> items = o.getOrderItems().stream().map(i -> {
            // Safely resolve generator fields — the referenced Generator may have been
            // soft-deleted or orphaned since the order was created. Accessing a Hibernate
            // proxy for a missing entity throws EntityNotFoundException at initialization
            // time, so we guard each access individually.
            Long generatorId = null;
            String generatorCode = null;
            try {
                if (i.getGenerator() != null) {
                    generatorId = i.getGenerator().getId();
                    generatorCode = i.getGenerator().getGeneratorCode();
                }
            } catch (jakarta.persistence.EntityNotFoundException ignored) {
                // Generator was deleted; leave generatorId/generatorCode as null
            }

            return OrderResponse.OrderItemResponse.builder()
                .id(i.getId())
                .generatorId(generatorId)
                .generatorName(i.getProductName())
                .generatorCode(generatorCode)
                .cableSize(i.getCableSize())
                .cableRate(i.getCableRate())
                .startTime(i.getStartTime())
                .endTime(i.getEndTime())
                .duration(i.getDuration())
                .rate(i.getRate())
                .dieselRate(i.getDieselRate())
                .totalAmount(i.getTotalAmount())
                .dieselEntries(i.getDieselEntries().stream().map(de ->
                    OrderResponse.DieselEntryResponse.builder()
                        .id(de.getId())
                        .entryDate(de.getEntryDate())
                        .startTime(de.getStartTime())
                        .endTime(de.getEndTime())
                        .duration(de.getDuration())
                        .build()
                ).collect(Collectors.toList()))
                .build();
        }).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(o.getId())
                .orderNumber(o.getOrderNumber())
                .billNumber(o.getBillNumber())
                .clientName(o.getCustomer() != null ? o.getCustomer().getName() : null)
                .contactNumber(o.getCustomer() != null ? o.getCustomer().getMobile() : null)
                .alternateMobile(o.getAlternateMobile())
                .operatorName(o.getOperatorName())
                .operatorMobile(o.getOperatorMobile())
                .cableRequired(o.getCableRequired())
                .dieselType(Boolean.TRUE.equals(o.getWithDiesel()) ? "WITH_OWNER" : "PARTY")
                .siteAddress(o.getSiteAddress())
                .siteAddressLink(o.getSiteAddressLink())
                .remarks(o.getNotes())
                .functionDateFrom(o.getFunctionDateFrom())
                .functionDateTo(o.getFunctionDateTo())
                .functionDate(funcDate)
                .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus().name() : null)
                .billingStatus(o.getBillingStatus() != null ? o.getBillingStatus().name() : null)
                .paymentDueDate(o.getPaymentDueDate())
                .paymentStatus(o.getPaymentStatus() != null ? o.getPaymentStatus().name() : PaymentStatus.PENDING.name())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .taxAmount(o.getTaxAmount())
                .finalAmount(o.getFinalAmount())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .generators(items)
                .build();
    }

    @Override
    @Transactional
    public OrderResponse updateBilling(Long id, UpdateOrderBillingRequest request) {
        Order order = findOrderOrThrow(id);
        
        if (order.getBillingStatus() == BillingStatus.COMPLETED) {
            throw new AppException("Cannot update billing for a completed bill.", HttpStatus.BAD_REQUEST);
        }

        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        order.setDiscountAmount(discount);

        BigDecimal subtotal = BigDecimal.ZERO;

        if (request.getGenerators() != null) {
            for (UpdateOrderBillingRequest.BillingItemRequest itemReq : request.getGenerators()) {
                OrderItem item = order.getOrderItems().stream()
                        .filter(i -> i.getId().equals(itemReq.getOrderItemId()))
                        .findFirst()
                        .orElseThrow(() -> new AppException("Order item not found with id: " + itemReq.getOrderItemId(), HttpStatus.NOT_FOUND));

                item.setRate(itemReq.getRentPerDay());
                item.setDieselRate(itemReq.getDieselPerHour());
                if (itemReq.getCableRate() != null) {
                    item.setCableRate(itemReq.getCableRate());
                }

                // Update diesel entries
                item.getDieselEntries().clear();
                if (itemReq.getDieselEntries() != null) {
                    for (UpdateOrderBillingRequest.DieselEntryRequest deReq : itemReq.getDieselEntries()) {
                        OrderItemDieselEntry de = new OrderItemDieselEntry();
                        de.setEntryDate(deReq.getEntryDate());
                        de.setStartTime(deReq.getStartTime());
                        de.setEndTime(deReq.getEndTime());
                        de.setDuration(deReq.getDuration());
                        item.addDieselEntry(de);
                    }
                }

                long days = 1;
                if(order.getFunctionDateFrom() != null && order.getFunctionDateTo() != null) {
                    days = java.time.temporal.ChronoUnit.DAYS.between(order.getFunctionDateFrom(), order.getFunctionDateTo()) + 1;
                }
                
                BigDecimal totalRent = item.getRate() != null ? item.getRate().multiply(BigDecimal.valueOf(days)) : BigDecimal.ZERO;
                BigDecimal totalDiesel = BigDecimal.ZERO;
                if (item.getDieselRate() != null) {
                    for (OrderItemDieselEntry de : item.getDieselEntries()) {
                        if (de.getDuration() != null) {
                            totalDiesel = totalDiesel.add(de.getDuration().multiply(item.getDieselRate()));
                        }
                    }
                }
                BigDecimal totalCable = BigDecimal.ZERO;
                if (Boolean.TRUE.equals(order.getCableRequired()) && item.getCableRate() != null) {
                    totalCable = item.getCableRate().multiply(BigDecimal.valueOf(days));
                }

                item.setTotalAmount(totalRent.add(totalDiesel).add(totalCable));
                subtotal = subtotal.add(item.getTotalAmount());
            }
        }

        order.setSubtotal(subtotal);
        BigDecimal finalAmt = subtotal.subtract(discount).max(BigDecimal.ZERO);
        order.setFinalAmount(finalAmt);

        // Set paymentDueDate: use supplied value, or default to today + 7 days if not yet set
        if (request.getPaymentDueDate() != null) {
            order.setPaymentDueDate(request.getPaymentDueDate());
        } else if (order.getPaymentDueDate() == null) {
            order.setPaymentDueDate(LocalDate.now().plusDays(7));
        }

        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    @Override
    @Transactional
    public OrderResponse completeBilling(Long id) {
        Order order = findOrderOrThrow(id);
        if (order.getBillingStatus() == BillingStatus.COMPLETED) {
            throw new AppException("Billing is already completed.", HttpStatus.BAD_REQUEST);
        }
        order.setBillingStatus(BillingStatus.COMPLETED);
        if (order.getBillNumber() == null) {
            order.setBillNumber(generateBillNumber());
        }
        if (order.getOrderStatus() == OrderStatus.PENDING) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }
        // Set default due date on completion if not already set
        if (order.getPaymentDueDate() == null) {
            order.setPaymentDueDate(LocalDate.now().plusDays(7));
        }
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    @Override
    @Transactional
    public OrderResponse markPaymentDone(Long id) {
        Order order = findOrderOrThrow(id);
        order.setPaymentStatus(PaymentStatus.PAID);
        Order saved = orderRepository.save(order);
        return toDto(saved);
    }

    private String generateBillNumber() {
        long count = orderRepository.countByBillNumberNotNull();
        long seq = count + 1;
        while (true) {
            String candidate = formatBillNumber(seq);
            if (!orderRepository.existsByBillNumber(candidate)) {
                return candidate;
            }
            seq++;
        }
    }

    private String formatBillNumber(long seq) {
        long group = (seq - 1) / 1000;
        long num = (seq - 1) % 1000 + 1;
        if (group == 0) {
            return String.format("%05d", num);
        } else {
            char letter = (char) ('A' + (group - 1));
            return letter + String.format("%04d", num);
        }
    }
}
