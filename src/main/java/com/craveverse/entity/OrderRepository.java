
package com.craveverse.entity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
@Query("""
    SELECT DISTINCT o
    FROM Order o
    LEFT JOIN FETCH o.items i
    LEFT JOIN FETCH i.food
    WHERE o.customer.id = :customerId
    """)
List<Order> findByCustomer_Id(@Param("customerId") Long customerId);
    @Override
    @EntityGraph(attributePaths = {"items", "items.food"})
    List<Order> findAll();

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.food " +
           "WHERE o.id = :id")
    Optional<Order> findOrderWithItemsById(@Param("id") Long id);
}
