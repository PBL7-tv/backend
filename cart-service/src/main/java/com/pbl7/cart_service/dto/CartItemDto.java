package com.pbl7.cart_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartItemDto {
    private String productId;
    private String productName;
    private Long productPrice;
    private Integer quantity;
    private Long totalPrice;
    private List<String> images;
}
