package com.example.inventory_service.dto;

import lombok.Data;

@Data
public class InventoryDTO {
    Long id;

    Long productId;
    Integer quantity;
}
