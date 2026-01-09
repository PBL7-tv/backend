package com.pbl7.order_service.repository;

import com.pbl7.order_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findOrdersByUserId(String userId);

    @Query("SELECT o FROM Order o WHERE FUNCTION('YEAR', o.createdAt) = :year AND o.orderStatus = 'DELIVERED'")
    List<Order> findDeliveredOrdersByYear(@Param("year") int year);
}
