package com.pbl7.identity_service.repository.httpclient;

import com.pbl7.identity_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.identity_service.dto.ApiResponse;
import com.pbl7.identity_service.dto.request.CreateCartRequest;
import com.pbl7.identity_service.dto.response.CreateCartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cart-service", url = "${app.services.cart}",
        configuration = { AuthenticationRequestInterceptor.class })
public interface CartClient {
    @PostMapping(value = "/internal/create", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<CreateCartResponse> createCart(@RequestBody CreateCartRequest request);
}
