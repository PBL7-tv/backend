package com.pbl7.product_service.service;


import com.pbl7.product_service.dto.CategoryDTO;
import com.pbl7.product_service.dto.InventoryDTO;
import com.pbl7.product_service.dto.request.CreateProductRequest;
import com.pbl7.product_service.dto.request.InventoryUpdateRequest;
import com.pbl7.product_service.dto.request.UpdateProductRequest;
import com.pbl7.product_service.dto.response.CreateProductResponse;
import com.pbl7.product_service.entity.Product;
import com.pbl7.product_service.exception.AppException;
import com.pbl7.product_service.exception.ErrorCode;
import com.pbl7.product_service.mapper.ProductMapper;
import com.pbl7.product_service.repository.ProductRepository;
import com.pbl7.product_service.repository.httpclient.CategoryClient;
import com.pbl7.product_service.repository.httpclient.InventoryClient;
import feign.FeignException;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {

    ProductRepository productRepository;
    CategoryClient categoryClient;
    InventoryClient inventoryClient;
    ProductMapper productMapper;

    @Override
    public CreateProductResponse createProduct(CreateProductRequest request) {
        log.info("Create product request: {}", request.getCategoryId());
        CategoryDTO category = categoryClient.getCategory(request.getCategoryId()).getBody();

        if (category == null) {
            category = categoryClient.createCategory(
                    CategoryDTO.builder().categoryId(request.getCategoryId()).build()
            ).getBody();

            if (category == null) {
                throw new AppException(ErrorCode.CATEGORY_CREATION_FAILED);
            }
        }

        Product product = productMapper.toProduct(request);
        product.setCategoryId(category.getCategoryId());
        product.setCreatedAt(LocalDateTime.now());
        product.setBrand(request.getBrand());
        product.setImages(request.getImages());
        product.setSellingPrice(request.getSellingPrice());
        // Các trường khác đã được map tự động

        productRepository.save(product);

        InventoryDTO inventoryDTO = InventoryDTO.builder()
                .quantity(request.getQuantity())
                .productId(product.getId())
                .build();

        log.info("Create product: {}", inventoryDTO);

        inventoryClient.create(inventoryDTO);

        return productMapper.toCreateProductResponse(product);
    }

    private int calculateDiscountPercentage(int mrpPrice, int sellingPrice) {
        if (mrpPrice <= 0) {
            throw new IllegalArgumentException("MrpPrice must be greater than 0");
        }

        double discount = mrpPrice - sellingPrice;
        double percentage = (discount / mrpPrice) * 100;

        return (int) percentage;
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = this.findProductById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        this.findProductById(id);
        product.setId(id);
        return productRepository.save(product);
    }

    @Override
    public Page<Product> getAllProducts(String category, String brand, String colors, Integer minPrice, Integer maxPrice, String sort, Integer pageNumber) {
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null) {
//                Join<Product, Category> categoryJoin = root.join("category");
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), category));
            }

            if (colors != null && !colors.isEmpty()) {
//                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("color")), colors.toLowerCase()));
                predicates.add(criteriaBuilder.equal(root.get("color"), colors));
            }

            if (brand != null && !brand.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("brand"), brand));
            }

            if (minPrice != null && minPrice > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }
            if (maxPrice != null && maxPrice > 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }


            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable;
        if (sort != null && !sort.isEmpty()) {
            pageable = switch (sort) {
                case "price_low" -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.by("sellingPrice").ascending());
                case "price_high" -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.by("sellingPrice").descending());
                default -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10,
                        Sort.unsorted());
            };
        } else {
            pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, 8, Sort.unsorted());
        }
        return productRepository.findAll(specification, pageable);
    }

    @Override
    public List<Product> searchProducts(String query) {
        return productRepository.searchProduct(query);
    }

    @Override
    public Product findProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, UpdateProductRequest request) {
        Product existingProduct = this.findProductById(id);

        productMapper.updateProductFromRequest(request, existingProduct);
        // Các trường khác nếu cần

        // Nếu có cập nhật quantity, gọi sang inventory service
        if (request.getQuantity() >= 0) {
            try {
                InventoryUpdateRequest inventoryRequest = new InventoryUpdateRequest();
                inventoryRequest.setProductId(existingProduct.getId());
                inventoryRequest.setQuantity(request.getQuantity());
                inventoryClient.updateQuantityOfProduct(inventoryRequest);
            } catch (FeignException.BadRequest e) {
                // Nếu lỗi "Product out of stock", bỏ qua việc cập nhật quantity
                log.warn("Failed to update product quantity: {}", e.getMessage());
            }
        }

        return productRepository.save(existingProduct);
    }


}
