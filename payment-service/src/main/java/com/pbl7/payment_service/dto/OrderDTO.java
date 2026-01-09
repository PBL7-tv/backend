package com.pbl7.payment_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderDTO {
    private Long id;

    private String userId;

    private Long totalAmount;
    private String orderStatus;
    private Instant createdAt;

    private List<OrderItemDto> items;

    private int totalItem;
    private Long totalPrice;
}
