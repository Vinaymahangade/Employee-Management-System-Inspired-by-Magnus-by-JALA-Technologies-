package com.jala.empmanagement.repository;

import com.jala.empmanagement.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository layer for Employee entity.
 * Extends JpaRepository to get standard CRUD + pagination support.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // ── Auth Queries ───────────────────────────────────────────────────────

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    // ── Search, Filter & Pagination (combined) ────────────────────────────

    /**
     * Dynamic search combining name/email keyword, status filter, and role filter.
     * All parameters are optional — null values are ignored via COALESCE logic.
     */
    @Query("""
        SELECT e FROM Employee e
        WHERE (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                                OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:status IS NULL OR e.status = :status)
          AND (:role IS NULL OR e.role = :role)
        """)
    Page<Employee> searchEmployees(
        @Param("keyword") String keyword,
        @Param("status") Employee.Status status,
        @Param("role") Employee.Role role,
        Pageable pageable
    );

    // ── Autocomplete Suggestions ───────────────────────────────────────────

    /**
     * Returns top 10 name/email suggestions for autocomplete widget.
     */
    @Query("""
        SELECT e FROM Employee e
        WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.email) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY e.name ASC
        """)
    List<Employee> findSuggestions(@Param("query") String query, Pageable pageable);

    // ── Dashboard Stats ────────────────────────────────────────────────────

    long countByStatus(Employee.Status status);

    long countByRole(Employee.Role role);

    @Query("SELECT e.department, COUNT(e) FROM Employee e GROUP BY e.department")
    List<Object[]> countByDepartment();
}
