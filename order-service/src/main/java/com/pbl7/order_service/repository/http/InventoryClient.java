package com.pbl7.order_service.repository.http;

import com.pbl7.order_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.order_service.dto.request.InventoryUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "inventory-service", url = "${app.services.inventory}",
        configuration = { AuthenticationRequestInterceptor.class })
public interface InventoryClient {

    @GetMapping("/inventory/isInStock")
    boolean isInStock(@RequestParam Long productId, @RequestParam int quantity);

    @PostMapping("/inventory/update")
    ResponseEntity<String> updateQuantity(@RequestBody InventoryUpdateRequest request);

    @PostMapping("/inventory/updateStock")
    ResponseEntity<String> updateStock(@RequestBody List<InventoryUpdateRequest> requests);

}
