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

            // Universal keyword search: name OR firmName OR mobile OR alternateMobile OR telephoneNumber OR address OR area OR city (case-insensitive LIKE)
            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().toLowerCase().trim() + "%";
                Predicate nameLike        = cb.like(cb.lower(root.get("name")),            pattern);
                Predicate firmNameLike    = cb.like(cb.lower(root.get("firmName")),        pattern);
                Predicate mobileLike      = cb.like(cb.lower(root.get("mobile")),          pattern);
                Predicate altMobileLike   = cb.like(cb.lower(root.get("alternateMobile")),  pattern);
                Predicate telephoneLike   = cb.like(cb.lower(root.get("telephoneNumber")), pattern);
                Predicate addressLike     = cb.like(cb.lower(root.get("address")),          pattern);
                Predicate areaLike        = cb.like(cb.lower(root.get("area")),             pattern);
                Predicate cityLike        = cb.like(cb.lower(root.get("city")),             pattern);
                predicates.add(cb.or(nameLike, firmNameLike, mobileLike, altMobileLike, telephoneLike, addressLike, areaLike, cityLike));
            }

            // Dedicated filter by firm name
            if (filter.getFirmName() != null && !filter.getFirmName().isBlank()) {
                String pattern = "%" + filter.getFirmName().toLowerCase().trim() + "%";
                predicates.add(cb.like(cb.lower(root.get("firmName")), pattern));
            }

            // Dedicated filter by mobile / telephone
            if (filter.getMobile() != null && !filter.getMobile().isBlank()) {
                String pattern = "%" + filter.getMobile().toLowerCase().trim() + "%";
                Predicate mobileLike    = cb.like(cb.lower(root.get("mobile")),          pattern);
                Predicate altMobileLike = cb.like(cb.lower(root.get("alternateMobile")),  pattern);
                Predicate telephoneLike = cb.like(cb.lower(root.get("telephoneNumber")), pattern);
                predicates.add(cb.or(mobileLike, altMobileLike, telephoneLike));
            }

            // Dedicated filter by location (address, area, city)
            if (filter.getLocation() != null && !filter.getLocation().isBlank()) {
                String pattern = "%" + filter.getLocation().toLowerCase().trim() + "%";
                Predicate addressLike = cb.like(cb.lower(root.get("address")), pattern);
                Predicate areaLike    = cb.like(cb.lower(root.get("area")),    pattern);
                Predicate cityLike    = cb.like(cb.lower(root.get("city")),    pattern);
                predicates.add(cb.or(addressLike, areaLike, cityLike));
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
                cb.like(cb.lower(root.get("name")),            pattern),
                cb.like(cb.lower(root.get("firmName")),        pattern),
                cb.like(cb.lower(root.get("mobile")),          pattern),
                cb.like(cb.lower(root.get("alternateMobile")),  pattern),
                cb.like(cb.lower(root.get("telephoneNumber")), pattern),
                cb.like(cb.lower(root.get("address")),          pattern),
                cb.like(cb.lower(root.get("area")),             pattern),
                cb.like(cb.lower(root.get("city")),             pattern)
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
