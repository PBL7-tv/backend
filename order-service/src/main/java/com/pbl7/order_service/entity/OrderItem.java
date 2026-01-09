package com.pbl7.order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_items")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
     Long id;

     Long productId;

     String productName;
     Long productPrice;
     String color;


     int quantity;

     Long totalPrice;

     @ElementCollection
     List<String> images = new ArrayList<>();
}
