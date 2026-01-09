package com.pbl7.product_service.repository;

import com.pbl7.product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

//    Đây là một truy vấn tìm kiếm linh hoạt, cho phép tìm theo title hoặc category.name.
//            LOWER() giúp tìm kiếm không phân biệt chữ hoa/thường

    @Query("SELECT p FROM Product p WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.categoryId) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProduct(@Param("query") String query);

}
