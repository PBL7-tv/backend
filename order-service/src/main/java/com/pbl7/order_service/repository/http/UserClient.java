package com.pbl7.order_service.repository.http;

import com.pbl7.order_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.order_service.dto.AddressDTO;
import com.pbl7.order_service.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "identity-service", url = "${app.services.identity}",  configuration = { AuthenticationRequestInterceptor.class })
public interface UserClient {
    @GetMapping("/identity/users/user/{userId}")
    UserDto getUser(@PathVariable("userId") String userId);


    @PostMapping("/identity/users/addresses")
    AddressDTO saveAddress(@RequestBody AddressDTO addressDTO);

    @PostMapping("/identity/users/{userId}/addresses")
    UserDto addAddressToUser(@PathVariable("userId") String userId, @RequestBody AddressDTO addressDTO);

    @GetMapping("/identity/users/addresses/{id}")
    AddressDTO getAddressById(@PathVariable("id") Long id);
}
