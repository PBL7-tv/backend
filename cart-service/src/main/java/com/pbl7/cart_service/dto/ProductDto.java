package com.pbl7.cart_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductDto {
    private String id;
    private String title;
    private Long sellingPrice;
    private List<String> images;
}
