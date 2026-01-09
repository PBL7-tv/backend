package com.pbl7.product_service.dto.request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProductRequest {
    String title;
    String description;
    String brand;
    Long sellingPrice;
    int quantity;
    String color;
    List<String> images;
    String categoryId;
    Map<String, Object> specs;
}
