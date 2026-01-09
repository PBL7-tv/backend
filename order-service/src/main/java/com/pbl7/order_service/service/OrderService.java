package com.pbl7.order_service.service;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.pbl7.event.NotificationEvent;
import com.pbl7.order_service.dto.*;
import com.pbl7.order_service.dto.request.InventoryUpdateRequest;
import com.pbl7.order_service.dto.request.OrderRequest;
import com.pbl7.order_service.dto.response.MonthlyOrderStatsResponse;
import com.pbl7.order_service.entity.Order;
import com.pbl7.order_service.entity.OrderItem;
import com.pbl7.order_service.exception.AppException;
import com.pbl7.order_service.exception.ErrorCode;
import com.pbl7.order_service.mapper.OrderMapper;
import com.pbl7.order_service.repository.OrderItemRepository;
import com.pbl7.order_service.repository.OrderRepository;
import com.pbl7.order_service.repository.http.CartClient;
import com.pbl7.order_service.repository.http.InventoryClient;
import com.pbl7.order_service.repository.http.UserClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrderRepository orderRepository;
    CartClient cartClient;
    OrderItemRepository orderItemRepository;
    UserClient userClient;
    KafkaTemplate<String, Object> kafkaTemplate;
    InventoryClient inventoryClient;
    OrderMapper orderMapper;


    public OrderDto createOrder(OrderRequest orderRequest, String jwt) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CartDto cartDto = cartClient.getCartByUserId(authentication.getName());

        Order order = Order.builder()
                .userId(authentication.getName())
                .createdAt(Instant.now())
                .orderStatus("PLACED")
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        List<InventoryUpdateRequest> requests = new ArrayList<>();


        for (CartItemDto cartItem : cartDto.getItems()) {


            var isInStock = inventoryClient.isInStock(cartItem.getProductId(), cartItem.getQuantity());

            if (isInStock) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setProductName(cartItem.getProductName());
                orderItem.setProductPrice(cartItem.getProductPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalPrice(cartItem.getTotalPrice());
                orderItem.setColor(cartItem.getColor());
                orderItem.setImages(cartItem.getImages());


                OrderItem savedOrderItem = orderItemRepository.save(orderItem);
                orderItems.add(savedOrderItem);

                requests.add(new InventoryUpdateRequest(cartItem.getProductId(), cartItem.getQuantity()));

            } else {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }


        }

        Long totalPrice = orderItems.stream()
                .mapToLong(OrderItem::getTotalPrice)
                .sum();

        order.setItems(orderItems);
        order.setTotalItem(orderItems.size());
        order.setTotalPrice(totalPrice);
        order.setAddressId(orderRequest.getDeliveryAddress().getId());

        Order savedOrder = orderRepository.save(order);

        inventoryClient.updateStock(requests);

        UserDto user = userClient.getUser(authentication.getName());

        AddressDTO shipAddress;

        if (orderRequest.getAddressId() != null) {
            shipAddress = userClient.getAddressById(orderRequest.getAddressId());
        } else {
            shipAddress = userClient.saveAddress(orderRequest.getDeliveryAddress());
        }

        boolean addressExists = user.getShipAddress() != null &&
                user.getShipAddress().stream().anyMatch(a -> a.getId().equals(shipAddress.getId()));


        if (!addressExists) {
            userClient.addAddressToUser(authentication.getName(), shipAddress);
        }

        log.info("user: {}", user);

        StringBuilder body = new StringBuilder("Thanks for your order! Here's your summary:\n");

        for (OrderItem item : savedOrder.getItems()) {
            body.append("- ").append(item.getProductName())
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(item.getTotalPrice()).append(" VND\n");
        }

        body.append("Total: ").append(savedOrder.getTotalPrice()).append(" VND\n");

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getEmail())
                .subject("Order Confirmation")
                .body(body.toString())
                .build();

//        NotificationEvent notificationEvent = NotificationEvent.builder()
//                .channel("EMAIL")
//                .recipient(user.getEmail())
//                .subject("Order Confirmation")
//                .body("Thank you for choosing us! Your order has been confirmed!")
//                .build();

        kafkaTemplate.send("VGearVN", notificationEvent);

        return orderMapper.toDto(savedOrder);
    }


    public String getIdFromJwt(String jwt) throws Exception {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        } else {
            throw new Exception("Invalid JWT token format");
        }

        JWSObject jwsObject = JWSObject.parse(jwt);

        Payload payload = jwsObject.getPayload();
        Map<String, Object> claimsMap = payload.toJSONObject();

        String userId = (String) claimsMap.get("sub");
        return userId;
    }

    public List<Order> getUserOrders(String userId) {
        return orderRepository.findOrdersByUserId(userId);
    }

    public List<Order> getRestaurantsOrder(String orderStatus) {

        List<Order> orders = orderRepository.findAll();

        if (orderStatus != null) {
            orders = orders.stream().filter(order -> order.getOrderStatus().equals(orderStatus)).collect(Collectors.toList());
        }
        return orders;
    }

    public Order updateOrder(Long orderId, String orderStatus) throws Exception {
        Order order = findOrderById(orderId);
        if (orderStatus.equals("OUT_FOR_DELIVERY") ||  orderStatus.equals("PLACED") || orderStatus.equals("SHIPPED") ||
                orderStatus.equals("DELIVERED") || orderStatus.equals("CONFIRMED")
                || orderStatus.equals("PENDING") || orderStatus.equals("ARRIVING")) {
            order.setOrderStatus(orderStatus);
            return orderRepository.save(order);
        }

        throw new Exception("Order status is not valid");
    }

    public Order findOrderById(Long orderId) throws Exception {
        Optional<Order> opt = orderRepository.findById(orderId);

        if (opt.isEmpty()) {
            throw new Exception("Order not found");
        }
        return opt.get();
    }

    public List<MonthlyOrderStatsResponse> getMonthlyOrderStats(int year) {
        List<Order> allOrders = orderRepository.findDeliveredOrdersByYear(year);

        return allOrders.stream()
            .collect(Collectors.groupingBy(
                order -> {
                    LocalDateTime orderDateTime = LocalDateTime.ofInstant(order.getCreatedAt(), ZoneId.systemDefault());
                    return orderDateTime.getMonthValue();
                },
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    orders -> {
                        if (orders.isEmpty()) {
                            return MonthlyOrderStatsResponse.builder()
                                .month(0)
                                .year(year)
                                .totalOrders(0)
                                .totalRevenue(0)
                                .totalItems(0)
                                .build();
                        }
                        
                        long totalOrders = orders.size();
                        long totalRevenue = orders.stream()
                            .mapToLong(Order::getTotalPrice)
                            .sum();
                        long totalItems = orders.stream()
                            .mapToInt(Order::getTotalItem)
                            .sum();

                        LocalDateTime firstOrderDateTime = LocalDateTime.ofInstant(orders.get(0).getCreatedAt(), ZoneId.systemDefault());
                        return MonthlyOrderStatsResponse.builder()
                            .month(firstOrderDateTime.getMonthValue())
                            .year(year)
                            .totalOrders(totalOrders)
                            .totalRevenue(totalRevenue)
                            .totalItems(totalItems)
                            .build();
                    }
                )
            ))
            .values()
            .stream()
            .sorted(Comparator.comparing(MonthlyOrderStatsResponse::getMonth))
            .collect(Collectors.toList());
    }

    public OrderDto cancelOrder(Long orderId, String jwt) throws Exception {
        Order order = findOrderById(orderId);
        if (order.getOrderStatus().equals("CANCELLED")) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND); 
        }
        order.setOrderStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);
        return orderMapper.toDto(cancelledOrder);
    }

}
