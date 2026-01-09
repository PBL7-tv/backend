package com.pbl7.product_service.repository.httpclient;

import com.pbl7.product_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.product_service.dto.ApiResponse;
import com.pbl7.product_service.dto.InventoryDTO;
import com.pbl7.product_service.dto.request.CreateInventoryRequest;
import com.pbl7.product_service.dto.request.InventoryUpdateRequest;
import com.pbl7.product_service.dto.response.CreateInventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "inventory-service", url = "${app.services.inventory}",
        configuration = { AuthenticationRequestInterceptor.class })
public interface InventoryClient {
    @PostMapping("/create")
    ApiResponse<CreateInventoryResponse> create(@RequestBody InventoryDTO request);

    @GetMapping("/{productId}")
    ResponseEntity<InventoryDTO> getInventoryByProductId(@PathVariable Long productId);

    @PostMapping("/update")
    ResponseEntity<String> updateQuantityOfProduct(@RequestBody InventoryUpdateRequest request);
}
