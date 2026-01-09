package com.pbl7.order_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItemDto {
     Long productId;
     String productName;
     Long productPrice;
     String color;
     Integer quantity;
     Long totalPrice;
     List<String> images = new ArrayList<>();
}
