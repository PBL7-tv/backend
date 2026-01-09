package com.pbl7.order_service.controller;

import com.pbl7.order_service.dto.ApiResponse;
import com.pbl7.order_service.dto.OrderDto;
import com.pbl7.order_service.dto.PaymentOrderDTO;
import com.pbl7.order_service.dto.request.OrderRequest;
import com.pbl7.order_service.entity.Order;
import com.pbl7.order_service.repository.http.PaymentClient;
import com.pbl7.order_service.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {
    OrderService orderService;
    PaymentClient paymentClient;

    @PostMapping("/create")
    ApiResponse<OrderDto> createOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String jwt) {

        return ApiResponse.<OrderDto>builder()
                .result(orderService.createOrder(orderRequest, jwt))
                .build();
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getOrderHistory(@RequestHeader("Authorization") String jwt) throws Exception {

        String userId = orderService.getIdFromJwt(jwt);
        List<Order> orders = orderService.getUserOrders(userId);

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId, @RequestHeader("Authorization") String jwt) throws Exception {

        Order order = orderService.findOrderById(orderId);

        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PutMapping("/cancel/{orderId}")
    public ApiResponse<OrderDto> cancelOrder(@PathVariable Long orderId, @RequestHeader("Authorization") String jwt) throws Exception {
        return ApiResponse.<OrderDto>builder()
                .result(orderService.cancelOrder(orderId, jwt))
                .build();
    }
}
