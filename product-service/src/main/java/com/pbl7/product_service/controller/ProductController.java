package com.pbl7.product_service.controller;


import com.pbl7.product_service.dto.request.CreateProductRequest;
import com.pbl7.product_service.dto.request.UpdateProductRequest;
import com.pbl7.product_service.dto.response.CreateProductResponse;
import com.pbl7.product_service.entity.Product;
import com.pbl7.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductService productService;


    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {

        Product product = productService.findProductById(productId);
        return new ResponseEntity<>(product, HttpStatus.OK);

    }

    @PostMapping("/add")
    public ResponseEntity<CreateProductResponse> createProduct(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateProductRequest req
    ) {
        log.info("Create product request: {}", req);
        CreateProductResponse response = productService.createProduct(req);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/update/{productId}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long productId,
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateProductRequest req
    ) {

        Product product = productService.updateProduct(productId, req);

        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(@RequestParam(required = false) String query){

        List<Product> products = productService.searchProducts(query);
        return new ResponseEntity<>(products, HttpStatus.OK);

    }

    @GetMapping("/find-all")
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") Integer pageNumber
    ){


        return new ResponseEntity<>(productService.getAllProducts(
                category, brand, color, maxPrice, minPrice,  sort,  pageNumber), HttpStatus.OK);

    }


}
