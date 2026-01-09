package com.example.inventory_service.mapper;

import com.example.inventory_service.dto.InventoryDTO;
import com.example.inventory_service.dto.request.CreateInventoryRequest;
import com.example.inventory_service.dto.response.CreateInventoryResponse;
import com.example.inventory_service.entity.Inventory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    Inventory toInventory(InventoryDTO request);
    CreateInventoryResponse toCreateInventoryResponse(Inventory inventory);
}
