package com.pbl7.order_service.dto.response;


import com.pbl7.order_service.entity.OrderItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOrderResponse {
     Long id;
     String orderStatus;
     Instant createdAt;
     List<OrderItem> items;
     int totalItem;
     Long totalPrice;
     String username;
     Long addressId;
}
