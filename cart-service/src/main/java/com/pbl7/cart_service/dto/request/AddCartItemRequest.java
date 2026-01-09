package com.pbl7.cart_service.dto.request;

import lombok.Data;


@Data
public class AddCartItemRequest {
    private String productId;
    private int quantity;
}
