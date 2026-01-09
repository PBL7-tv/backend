package com.pbl7.product_service.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateProductResponse {
    Long id;
    String title;
    String description;
    Long sellingPrice;
    String color;
    List<String> images;
    String categoryId;
    String brand;
    Integer quantity;
    Map<String, Object> specs;
}
