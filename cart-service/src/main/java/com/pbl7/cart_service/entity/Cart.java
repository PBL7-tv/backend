package com.pbl7.cart_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
     Long id;

     String userId;

    private Long totalSellingPrice;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Override
    public String toString() {
        return "Cart{id=" + id + ", userId=" + userId + ", totalSellingPrice=" + totalSellingPrice + ", itemsCount=" + (items != null ? items.size() : 0) + "}";
    }


}
