package com.pbl7.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
     String userId;

     Long totalAmount;
     String orderStatus;
     Instant createdAt;

     int totalItem;
     Long totalPrice;
}
