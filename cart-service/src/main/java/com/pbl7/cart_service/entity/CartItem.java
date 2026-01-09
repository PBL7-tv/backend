package com.pbl7.cart_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JsonIgnore
    private Cart cart;

    // Storing product details from Product Service
    String productId;  // Reference to the Product ID from Product Service
    String productName; // Product name
    Long productPrice; // Product price

    Integer quantity; // Quantity of the product in the cart

    // Total price for this cart item (productPrice * quantity)
    Long totalPrice;

    @ElementCollection
    List<String> images;

    @Override
    public String toString() {
        return "CartItem{id=" + id +
                ", productName=" + (productName != null ? productName : "null") +
                ", productPrice=" + productPrice +
                ", quantity=" + quantity +
                ", images=" + (images != null ? images : "no images") + "}";
    }


}
