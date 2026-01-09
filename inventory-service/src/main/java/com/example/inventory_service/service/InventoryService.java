package com.example.inventory_service.service;

import com.example.inventory_service.dto.InventoryDTO;
import com.example.inventory_service.dto.request.CreateInventoryRequest;
import com.example.inventory_service.dto.request.InventoryUpdateRequest;
import com.example.inventory_service.dto.response.CreateInventoryResponse;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.exception.AppException;
import com.example.inventory_service.exception.ErrorCode;
import com.example.inventory_service.mapper.InventoryMapper;
import com.example.inventory_service.repository.InventoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InventoryService {

    InventoryRepository inventoryRepository;
    InventoryMapper inventoryMapper;

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    public CreateInventoryResponse createInventory(InventoryDTO request) {
        Inventory inventory = inventoryMapper.toInventory(request);

        inventory = inventoryRepository.save(inventory);

        return inventoryMapper.toCreateInventoryResponse(inventory);
    }

    public boolean isInStock(Long productId, int quantity) {
        return inventoryRepository.existsByProductIdAndQuantityIsGreaterThanEqual(
                productId, quantity
        );
    }

    public void updateStock(List<InventoryUpdateRequest> requests) {
        for (InventoryUpdateRequest request : requests) {
            Inventory inventory = inventoryRepository.findByProductId(request.getProductId()).orElseThrow(
                    () -> new RuntimeException("Product not found")
            );
            if (inventory.getQuantity() < request.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
            inventoryRepository.save(inventory);


        }
    }

    public void updateQuantity(InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId()).orElseThrow(
                () -> new RuntimeException("Product not found")
        );

        inventory.setQuantity(request.getQuantity());
        inventoryRepository.save(inventory);

    }


}
