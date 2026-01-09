package com.pbl7.order_service.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDto {
     Long id;

     String userId;

     Long totalAmount;
     String orderStatus;
     Instant createdAt;

     List<OrderItemDto> items;

     int totalItem;
     Long totalPrice;
     Long addressId;
}
