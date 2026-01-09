package com.pbl7.cart_service.repository;

import com.pbl7.cart_service.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
