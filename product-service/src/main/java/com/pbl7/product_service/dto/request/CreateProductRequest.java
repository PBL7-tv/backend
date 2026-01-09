package com.pbl7.product_service.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductRequest {
    String title;
    String description;
//    int mrpPrice;
    Long sellingPrice;
    String color;
    List<String> images;
    String categoryId;
    String brand;
    Integer quantity;
    Map<String, Object> specs;
//    String category2;
//    String category3;
//    String sizes;
}
