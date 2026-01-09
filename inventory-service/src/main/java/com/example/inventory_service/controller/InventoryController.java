package com.example.inventory_service.controller;

import com.example.inventory_service.dto.ApiResponse;
import com.example.inventory_service.dto.InventoryDTO;
import com.example.inventory_service.dto.request.CreateInventoryRequest;
import com.example.inventory_service.dto.request.InventoryUpdateRequest;
import com.example.inventory_service.dto.response.CreateInventoryResponse;
import com.example.inventory_service.entity.Inventory;
import com.example.inventory_service.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InventoryController {

    InventoryService inventoryService;

    @GetMapping("/all")
    public ResponseEntity<List<Inventory>> findAll() {
        return new ResponseEntity<>(inventoryService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/isInStock")
    public boolean isInStock(@RequestParam Long productId, @RequestParam int quantity) {
        return inventoryService.isInStock(productId, quantity);
    }

    @PostMapping("/updateStock")
    public ResponseEntity<String> updateStock(@RequestBody List<InventoryUpdateRequest> requests) {
        inventoryService.updateStock(requests);
        return new ResponseEntity<>("Stock updated successfully",HttpStatus.OK);
    }

    @PostMapping("/create")
    public ApiResponse<CreateInventoryResponse> create(@RequestBody InventoryDTO request) {
        return ApiResponse.<CreateInventoryResponse>builder()
                .result(inventoryService.createInventory(request))
                .build();
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateQuantityOfProduct(@RequestBody InventoryUpdateRequest request) {
        inventoryService.updateQuantity(request);
        return new ResponseEntity<>("Product quantity updated successfully",HttpStatus.OK);
    }


}
