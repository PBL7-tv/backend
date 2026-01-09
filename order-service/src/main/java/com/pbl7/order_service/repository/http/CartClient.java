package com.pbl7.order_service.repository.http;

import com.pbl7.order_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.order_service.dto.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", url = "${app.services.cart}",  configuration = { AuthenticationRequestInterceptor.class })
public interface CartClient {
    @GetMapping("/cart/internal/user/{userId}")
    CartDto getCartByUserId(@PathVariable("userId") String userId);
}
