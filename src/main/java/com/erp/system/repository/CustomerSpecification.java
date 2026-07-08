package com.erp.system.repository;

import com.erp.system.dto.request.CustomerFilterRequest;
import com.erp.system.entity.Customer;
import com.erp.system.enums.CustomerStatus;
import com.erp.system.enums.CustomerType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Static factory for JPA Specifications used in customer search &amp; filter.
 * Each predicate is independently composable via {@link Specification#where}.
 *
 * <p>Usage example:
 * <pre>
 *   Specification&lt;Customer&gt; spec = CustomerSpecification.from(filterRequest);
 *   Page&lt;Customer&gt; page = customerRepository.findAll(spec, pageable);
 * </pre>
 */
public final class CustomerSpecification {

    private CustomerSpecification() {
        // utility class — no instances
    }

    // ── Composite builder ─────────────────────────────────────────────────

    /**
     * Builds a composite {@link Specification} from a {@link CustomerFilterRequest}.
     * Always applies the {@code notDeleted()} predicate.
     */
    public static Specification<Customer> from(CustomerFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always exclude soft-deleted records
            predicates.add(cb.isFalse(root.get("deleted")));

            if (filter == null) {
                return cb.and(predicates.toArray(new Predicate[0]));
            }

            // Keyword search: name OR mobile OR area (case-insensitive LIKE)
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().toLowerCase().trim() + "%";
                Predicate nameLike   = cb.like(cb.lower(root.get("name")),   pattern);
                Predicate mobileLike = cb.like(cb.lower(root.get("mobile")), pattern);
                Predicate areaLike   = cb.like(cb.lower(root.get("area")),   pattern);
                predicates.add(cb.or(nameLike, mobileLike, areaLike));
            }

            // CustomerType filter
            if (filter.getCustomerType() != null) {
                predicates.add(cb.equal(root.get("customerType"), filter.getCustomerType()));
            }

            // CustomerStatus filter
            if (filter.getCustomerStatus() != null) {
                predicates.add(cb.equal(root.get("customerStatus"), filter.getCustomerStatus()));
            }

            // isActive flag
            if (filter.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filter.getIsActive()));
            }

            // isRegular flag
            if (filter.getIsRegular() != null) {
                predicates.add(cb.equal(root.get("isRegular"), filter.getIsRegular()));
            }

            // Exact area match (case-insensitive)
            if (filter.getArea() != null && !filter.getArea().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("area")),
                        filter.getArea().toLowerCase().trim()
                ));
            }

            // Exact city match (case-insensitive)
            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("city")),
                        filter.getCity().toLowerCase().trim()
                ));
            }

            // dateJoined range
            if (filter.getDateJoinedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("dateJoined"), filter.getDateJoinedFrom()
                ));
            }
            if (filter.getDateJoinedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("dateJoined"), filter.getDateJoinedTo()
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // ── Individual predicate factories (for composing ad-hoc specs) ───────

    public static Specification<Customer> notDeleted() {
        return (root, q, cb) -> cb.isFalse(root.get("deleted"));
    }

    public static Specification<Customer> hasSearch(String search) {
        if (search == null || search.isBlank()) return null;
        String pattern = "%" + search.toLowerCase().trim() + "%";
        return (root, q, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")),   pattern),
                cb.like(cb.lower(root.get("mobile")), pattern),
                cb.like(cb.lower(root.get("area")),   pattern)
        );
    }

    public static Specification<Customer> hasCustomerType(CustomerType type) {
        if (type == null) return null;
        return (root, q, cb) -> cb.equal(root.get("customerType"), type);
    }

    public static Specification<Customer> hasCustomerStatus(CustomerStatus status) {
        if (status == null) return null;
        return (root, q, cb) -> cb.equal(root.get("customerStatus"), status);
    }

    public static Specification<Customer> isActive(Boolean active) {
        if (active == null) return null;
        return (root, q, cb) -> cb.equal(root.get("isActive"), active);
    }

    public static Specification<Customer> isRegular(Boolean regular) {
        if (regular == null) return null;
        return (root, q, cb) -> cb.equal(root.get("isRegular"), regular);
    }

    public static Specification<Customer> hasArea(String area) {
        if (area == null || area.isBlank()) return null;
        return (root, q, cb) -> cb.equal(cb.lower(root.get("area")), area.toLowerCase().trim());
    }

    public static Specification<Customer> hasCity(String city) {
        if (city == null || city.isBlank()) return null;
        return (root, q, cb) -> cb.equal(cb.lower(root.get("city")), city.toLowerCase().trim());
    }

    public static Specification<Customer> dateJoinedFrom(LocalDate from) {
        if (from == null) return null;
        return (root, q, cb) -> cb.greaterThanOrEqualTo(root.get("dateJoined"), from);
    }

    public static Specification<Customer> dateJoinedTo(LocalDate to) {
        if (to == null) return null;
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("dateJoined"), to);
    }
}
