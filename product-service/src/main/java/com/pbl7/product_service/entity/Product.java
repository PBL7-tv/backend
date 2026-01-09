package com.pbl7.product_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    String title;

    @Column(columnDefinition = "TEXT")
    String description;
    String brand;


//    int mrpPrice;

    Long sellingPrice;

//    int discountPercentage;

    String color;

    // tao mot table rieng
    @ElementCollection
    List<String> images = new ArrayList<>();

//    int numRatings;

    String categoryId;
    LocalDateTime createdAt;

    @Column(columnDefinition = "TEXT")
    String specs;

//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
//    List<Review> reviews = new ArrayList<>();

}
