package com.pbl7.order_service.repository.http;

import com.pbl7.order_service.configuration.AuthenticationRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "payment-service", url = "${app.services.payment}",  configuration = { AuthenticationRequestInterceptor.class })
public interface PaymentClient {

}
