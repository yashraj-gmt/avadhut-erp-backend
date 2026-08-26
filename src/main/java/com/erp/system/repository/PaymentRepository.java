package com.erp.system.repository;

import com.erp.system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Most recent payment date for a customer — used in pending-payment view. */
    @Query("""
        SELECT MAX(p.paymentDate)
        FROM   Payment p
        WHERE  p.customer.id = :customerId
        AND    p.deleted = false
        """)
    Optional<LocalDate> findLastPaymentDateByCustomerId(@Param("customerId") Long customerId);

    /**
     * Bulk fetch of last payment dates for multiple customers.
     * Result columns: customerId, maxPaymentDate.
     */
    @Query("""
        SELECT p.customer.id AS customerId, MAX(p.paymentDate) AS maxPaymentDate
        FROM   Payment p
        WHERE  p.customer.id IN :customerIds
        AND    p.deleted = false
        GROUP BY p.customer.id
        """)
    List<Object[]> findLastPaymentDatesByCustomerIds(
            @Param("customerIds") List<Long> customerIds);

    /** All payments for an order ordered chronologically. */
    List<Payment> findByOrderIdOrderByPaymentDateDescCreatedAtDesc(Long orderId);

    /** All payments for a customer ordered chronologically — for history timeline. */
    @Query("""
        SELECT p FROM Payment p
        WHERE  p.customer.id = :customerId
        AND    p.deleted = false
        ORDER BY p.paymentDate DESC, p.createdAt DESC
        """)
    List<Payment> findHistoryByCustomerId(@Param("customerId") Long customerId);
}
