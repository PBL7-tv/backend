package com.pbl7.category_service.repository;

import com.pbl7.category_service.dto.CategoryDTO;
import com.pbl7.category_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findCategoryByCategoryId(String categoryId);
}
