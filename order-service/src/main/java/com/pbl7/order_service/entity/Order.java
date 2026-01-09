package com.pbl7.order_service.entity;

import com.pbl7.order_service.dto.AddressDTO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
     Long id;

     String userId;

     Long totalAmount;
     String orderStatus;
     Instant createdAt;

    // One order can have multiple items
    @OneToMany
     List<OrderItem> items;

     int totalItem;
     Long totalPrice;

     Long addressId;

}
