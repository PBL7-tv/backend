package com.pbl7.cart_service.repository;

import com.pbl7.cart_service.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findCartByUserId(String userId);
}
