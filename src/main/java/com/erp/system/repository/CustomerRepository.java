package com.erp.system.repository;

import com.erp.system.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository
        extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    boolean existsByMobile(String mobile);

    boolean existsByMobileAndIdNot(String mobile, Long id);

    // ── Area-wise grouping ────────────────────────────────────────────────

    /**
     * Returns distinct (area, city) pairs with customer counts.
     * Result columns: area, city, totalCustomers, activeCustomers, regularCustomers.
     */
    @Query("""
        SELECT c.area                   AS area,
               c.city                   AS city,
               COUNT(c.id)              AS totalCustomers,
               SUM(CASE WHEN c.isActive = true  THEN 1 ELSE 0 END) AS activeCustomers,
               SUM(CASE WHEN c.isRegular = true THEN 1 ELSE 0 END) AS regularCustomers
        FROM   Customer c
        WHERE  c.deleted = false
        AND    (:city IS NULL OR LOWER(c.city) = LOWER(CAST(:city AS string)))
        GROUP BY c.area, c.city
        ORDER BY totalCustomers DESC
        """)
    Page<Object[]> findAreaSummary(@Param("city") String city, Pageable pageable);

    // ── Regular-customer recalculation ────────────────────────────────────

    /** Fetch all non-deleted, active customers for batch recalculation. */
    @Query("SELECT c FROM Customer c WHERE c.deleted = false AND c.isActive = true")
    List<Customer> findAllActiveForRecalc();

    // ── Pending-payments customer list ────────────────────────────────────

    /**
     * Returns customer IDs that have at least one invoice with PENDING or PARTIAL_PAID status.
     */
    @Query("""
        SELECT DISTINCT c.id
        FROM   Customer c
        JOIN   c.invoices i
        WHERE  c.deleted = false
        AND    i.deleted = false
        AND    i.paymentStatus IN ('PENDING', 'PARTIAL_PAID', 'OVERDUE')
        """)
    List<Long> findCustomerIdsWithPendingPayments();

    // ── Regular listing by total booking count ────────────────────────────

    /**
     * Returns customers ordered by their total order count descending
     * (most-frequent bookers first).
     */
    @Query("""
        SELECT c
        FROM   Customer c
        WHERE  c.deleted = false
        AND    c.isActive = true
        ORDER BY SIZE(c.orders) DESC
        """)
    Page<Customer> findAllOrderedByBookingCountDesc(Pageable pageable);
}
