package com.pbl7.cart_service.service;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.pbl7.cart_service.dto.ProductDto;
import com.pbl7.cart_service.dto.request.AddCartItemRequest;
import com.pbl7.cart_service.dto.request.CreateCartRequest;
import com.pbl7.cart_service.dto.response.CreateCartResponse;
import com.pbl7.cart_service.entity.Cart;
import com.pbl7.cart_service.entity.CartItem;
import com.pbl7.cart_service.mapper.CartMapper;
import com.pbl7.cart_service.repository.CartItemRepository;
import com.pbl7.cart_service.repository.CartRepository;
import com.pbl7.cart_service.repository.http.ProductClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {

    CartRepository cartRepository;
    CartMapper cartMapper;
    CartItemRepository cartItemRepository;
    ProductClient productClient;


    public CreateCartResponse createCart(CreateCartRequest request) {
        Cart cart = cartMapper.toCart(request);
        cart = cartRepository.save(cart);

        return cartMapper.toCreateCartResponse(cart);
    }

    public Cart findCartByUserId(String userId) throws Exception {
        Cart cart =  cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new Exception("Cart not found by userId: " + userId));
        cart.setTotalSellingPrice(calculateCartTotals(cart));
        return cart;
    }

    public Long calculateCartTotals(Cart cart)  {
        Long total = 0L;
        for(CartItem cartItem : cart.getItems()){
            total += cartItem.getProductPrice()*cartItem.getQuantity();
        }
        return total;
    }

    public CartItem addItemToCart(AddCartItemRequest req) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        ProductDto food = productClient.getFoodById(req.getProductId());
        log.info("Food images: {}", food.getImages());



        Cart cart = cartRepository.findCartByUserId(authentication.getName())
                .orElseThrow(() -> new Exception("Cart not found by userId: " + authentication.getName()));

        log.info("Cart retrieved: {}", cart);
        log.info("Cart items: {}", cart.getItems());

        for(CartItem cartItem : cart.getItems()){
            if(Objects.equals(cartItem.getProductId(), food.getId())){
                int newQuantity = cartItem.getQuantity() + req.getQuantity();
                return updateCartItemQuantity(cartItem.getId(), newQuantity);
            }
        }

        CartItem newCartItem = new CartItem();
        newCartItem.setProductId(req.getProductId());
        newCartItem.setProductName(food.getTitle());
        newCartItem.setQuantity(req.getQuantity());
        newCartItem.setProductPrice(food.getSellingPrice());
        newCartItem.setImages(food.getImages());
        newCartItem.setCart(cart);
        newCartItem.setTotalPrice(req.getQuantity()*food.getSellingPrice());


        CartItem savedCartItem = cartItemRepository.save(newCartItem);
        cart.getItems().add(savedCartItem);

        return savedCartItem;
    }

    public Cart clearCart(String userId) throws Exception {
        Cart cart = findCartByUserId(userId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }


    public CartItem updateCartItemQuantity(Long cartItemId, int quantity) throws Exception {
        Optional<CartItem> opt = cartItemRepository.findById(cartItemId);

        if(opt.isEmpty()){
            throw new Exception("Cart item not found");
        }
        CartItem item = opt.get();
        item.setQuantity(quantity);

        item.setTotalPrice(quantity*item.getProductPrice());

        return cartItemRepository.save(item);
    }

    public Cart removeItemFromCart(Long cartItemId, String jwt) throws Exception {

        String userId = getIdFromJwt(jwt);

        Cart cart = cartRepository.findCartByUserId(userId)
                .orElseThrow(() -> new Exception("Cart not found by userId: " + userId));

        Optional<CartItem> opt = cartItemRepository.findById(cartItemId);

        if(opt.isEmpty()){
            throw new Exception("Cart item not found");
        }
        CartItem item = opt.get();
        cart.getItems().remove(item);

        return cartRepository.save(cart);
    }

    public String getIdFromJwt(String jwt) throws Exception {
        if (jwt != null && jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        } else {
            throw new Exception("Invalid JWT token format");
        }

        JWSObject jwsObject = JWSObject.parse(jwt);

        Payload payload = jwsObject.getPayload();
        Map<String, Object> claimsMap = payload.toJSONObject();

        String userId = (String) claimsMap.get("sub");
        return userId;
    }


}
