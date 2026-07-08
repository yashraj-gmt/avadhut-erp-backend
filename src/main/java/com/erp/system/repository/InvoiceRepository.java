package com.erp.system.repository;

import com.erp.system.entity.Invoice;
import com.erp.system.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** All non-deleted invoices for a customer, newest first. */
    @Query("""
        SELECT i FROM Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        ORDER BY i.createdAt DESC
        """)
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    /** Latest N invoices for profile summary. */
    @Query("""
        SELECT i FROM Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        ORDER BY i.createdAt DESC
        LIMIT  :limit
        """)
    List<Invoice> findTopByCustomerId(@Param("customerId") Long customerId,
                                      @Param("limit") int limit);

    /** Unpaid / partially-paid invoices for a specific customer. */
    @Query("""
        SELECT i FROM Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        AND    i.paymentStatus IN ('PENDING', 'PARTIAL_PAID', 'OVERDUE')
        ORDER BY i.dueDate ASC
        """)
    List<Invoice> findUnpaidByCustomerId(@Param("customerId") Long customerId);

    /** All unpaid invoices for a list of customer IDs — used by pending-payment view. */
    @Query("""
        SELECT i FROM Invoice i
        WHERE  i.customer.id IN :customerIds
        AND    i.deleted = false
        AND    i.paymentStatus IN ('PENDING', 'PARTIAL_PAID', 'OVERDUE')
        """)
    List<Invoice> findUnpaidByCustomerIds(@Param("customerIds") List<Long> customerIds);

    /** Total invoice amount (sum of finalAmount) for a customer. */
    @Query("""
        SELECT COALESCE(SUM(i.finalAmount), 0)
        FROM   Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        """)
    BigDecimal sumFinalAmountByCustomerId(@Param("customerId") Long customerId);

    /** Total paid amount (sum of paidAmount) for a customer. */
    @Query("""
        SELECT COALESCE(SUM(i.paidAmount), 0)
        FROM   Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        """)
    BigDecimal sumPaidAmountByCustomerId(@Param("customerId") Long customerId);

    /** Count of invoices by status for a customer. */
    long countByCustomerIdAndPaymentStatusAndDeletedFalse(
            Long customerId, PaymentStatus paymentStatus);

    /** Total pending amount across all unpaid invoices for a customer. */
    @Query("""
        SELECT COALESCE(SUM(i.pendingAmount), 0)
        FROM   Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        AND    i.paymentStatus IN ('PENDING', 'PARTIAL_PAID', 'OVERDUE')
        """)
    BigDecimal sumPendingAmountByCustomerId(@Param("customerId") Long customerId);

    /** For history timeline: all invoices for a customer ordered chronologically. */
    @Query("""
        SELECT i FROM Invoice i
        WHERE  i.customer.id = :customerId
        AND    i.deleted = false
        ORDER BY i.createdAt DESC
        """)
    List<Invoice> findHistoryByCustomerId(@Param("customerId") Long customerId);

    /** Count orders placed by the customer within a date window (for regular algorithm). */
    @Query("""
        SELECT COUNT(o)
        FROM   Order o
        WHERE  o.customer.id = :customerId
        AND    o.deleted = false
        AND    o.createdAt >= :since
        """)
    long countOrdersSince(@Param("customerId") Long customerId,
                          @Param("since") java.time.LocalDateTime since);
}
