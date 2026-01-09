package com.pbl7.order_service.controller;

import com.pbl7.order_service.dto.OrderDto;
import com.pbl7.order_service.dto.request.OrderRequest;
import com.pbl7.order_service.entity.Order;
import com.pbl7.order_service.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/internal/order")
public class InternalOrderController {

    OrderService orderService;

    @GetMapping
    public ResponseEntity<Order> getOrderById(@RequestParam Long orderId) throws Exception {
        Order order = orderService.findOrderById(orderId);
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderRequest orderRequest, @RequestHeader("Authorization") String jwt) {

        OrderDto order =  orderService.createOrder(orderRequest, jwt );


        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}
