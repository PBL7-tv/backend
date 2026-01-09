package com.pbl7.cart_service.controller;


import com.pbl7.cart_service.dto.request.AddCartItemRequest;
import com.pbl7.cart_service.dto.request.UpdateCartItemRequest;
import com.pbl7.cart_service.entity.Cart;
import com.pbl7.cart_service.entity.CartItem;
import com.pbl7.cart_service.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartController {
    CartService cartService;

    @PutMapping("/add")
    public ResponseEntity<CartItem> addItemToCart(@RequestBody AddCartItemRequest req) throws Exception {
        CartItem cartItem = cartService.addItemToCart(req);

        return new ResponseEntity<>(cartItem, HttpStatus.OK);
    }

    @GetMapping("/user-cart")
    public ResponseEntity<Cart> findUserCart(@RequestHeader("Authorization") String jwt) throws Exception {
        String userId = cartService.getIdFromJwt(jwt);

        Cart cart = cartService.findCartByUserId(userId);

        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/clear")
    public ResponseEntity<Cart> clearCart( @RequestHeader("Authorization") String jwt) throws Exception {


        String userId = cartService.getIdFromJwt(jwt);

        Cart cart = cartService.clearCart(userId);

        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PutMapping("/cart-item/update")
    public ResponseEntity<CartItem> updateCartItemQuantity(@RequestBody UpdateCartItemRequest req, @RequestHeader("Authorization") String jwt) throws Exception {
        CartItem cartItem = cartService.updateCartItemQuantity(req.getCartItemId(), req.getQuantity());

        return new ResponseEntity<>(cartItem, HttpStatus.OK);
    }

    @DeleteMapping("/cart-item/{id}/remove")
    public ResponseEntity<Cart> removeCartItem (@PathVariable Long id, @RequestHeader("Authorization") String jwt) throws Exception {
        Cart cart = cartService.removeItemFromCart(id, jwt);

        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

}
