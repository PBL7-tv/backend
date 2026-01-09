package com.pbl7.cart_service.mapper;

import com.pbl7.cart_service.dto.CartDto;
import com.pbl7.cart_service.dto.request.CreateCartRequest;
import com.pbl7.cart_service.dto.response.CreateCartResponse;
import com.pbl7.cart_service.entity.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {
    Cart toCart(CreateCartRequest request);

    CreateCartResponse toCreateCartResponse(Cart cart);
    CartDto toCartDto(Cart cart);
}
