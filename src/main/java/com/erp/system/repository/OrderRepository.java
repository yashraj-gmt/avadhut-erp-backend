package com.erp.system.repository;

import com.erp.system.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Count non-deleted orders for a customer. */
    long countByCustomerIdAndDeletedFalse(Long customerId);

    /**
     * Count orders placed by a customer after a given cutoff — used by the
     * regular-customer algorithm to check the lookback window.
     */
    @Query("""
        SELECT COUNT(o)
        FROM   Order o
        WHERE  o.customer.id = :customerId
        AND    o.deleted = false
        AND    o.createdAt >= :since
        """)
    long countByCustomerIdSince(@Param("customerId") Long customerId,
                                @Param("since") LocalDateTime since);

    /**
     * Sum of finalAmount across all non-deleted orders for a customer —
     * used as an alternative qualifying threshold for the regular algorithm.
     */
    @Query("""
        SELECT COALESCE(SUM(o.finalAmount), 0)
        FROM   Order o
        WHERE  o.customer.id = :customerId
        AND    o.deleted = false
        """)
    BigDecimal sumFinalAmountByCustomerId(@Param("customerId") Long customerId);

    /** Latest N orders for profile summary. */
    @Query("""
        SELECT o FROM Order o
        WHERE  o.customer.id = :customerId
        AND    o.deleted = false
        ORDER BY o.createdAt DESC
        LIMIT  :limit
        """)
    List<Order> findTopByCustomerId(@Param("customerId") Long customerId,
                                    @Param("limit") int limit);

    /** All orders for a customer ordered chronologically — for history timeline. */
    @Query("""
        SELECT o FROM Order o
        WHERE  o.customer.id = :customerId
        AND    o.deleted = false
        ORDER BY o.createdAt DESC
        """)
    List<Order> findHistoryByCustomerId(@Param("customerId") Long customerId);
}
