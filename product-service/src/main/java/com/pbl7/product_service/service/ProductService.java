package com.pbl7.product_service.service;


import com.pbl7.product_service.dto.request.CreateProductRequest;
import com.pbl7.product_service.dto.request.UpdateProductRequest;
import com.pbl7.product_service.dto.response.CreateProductResponse;
import com.pbl7.product_service.entity.Product;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    CreateProductResponse createProduct(CreateProductRequest request);
    void deleteProduct(Long id) ;
    Product updateProduct(Long id, Product product) ;
    Page<Product> getAllProducts(String category, String brand, String colors,  Integer minPrice, Integer maxPrice,
            String sort,  Integer pageNumber);
    List<Product> searchProducts(String query);
    Product findProductById(Long id) ;
    Product updateProduct(Long id, UpdateProductRequest request) ;
}
