package com.pbl7.cart_service.controller;

import com.pbl7.cart_service.dto.ApiResponse;
import com.pbl7.cart_service.dto.CartDto;
import com.pbl7.cart_service.dto.request.CreateCartRequest;
import com.pbl7.cart_service.dto.response.CreateCartResponse;
import com.pbl7.cart_service.entity.Cart;
import com.pbl7.cart_service.mapper.CartMapper;
import com.pbl7.cart_service.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalCartController {

    CartService cartService;
    CartMapper cartMapper;

    @PostMapping("/internal/create")
    ApiResponse<CreateCartResponse> createProfile(@RequestBody CreateCartRequest request) {
        return ApiResponse.<CreateCartResponse>builder()
                .result(cartService.createCart(request))
                .build();
    }

    @GetMapping("/internal/user/{userId}")
    public ResponseEntity<CartDto> getCartByUserId(@PathVariable String userId) throws Exception {
        Cart cart = cartService.findCartByUserId(userId);
        CartDto cartDto = cartMapper.toCartDto(cart);
        return ResponseEntity.ok(cartDto);
    }
}
