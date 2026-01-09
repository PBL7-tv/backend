package com.pbl7.payment_service.repository.http;


import com.pbl7.payment_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.payment_service.dto.OrderDTO;
import com.pbl7.payment_service.dto.request.OrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", url = "${app.services.order}",  configuration = { AuthenticationRequestInterceptor.class })
public interface OrderClient {
    @PostMapping("/order/internal/order/create")
    OrderDTO createOrder(@RequestBody OrderRequest orderRequest);

    @GetMapping("/order/internal/order")
    OrderDTO getOrderById(@RequestParam Long orderId);
}
