package com.pbl7.category_service.repository;

import com.pbl7.category_service.entity.HomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeCategoryRepository extends JpaRepository<HomeCategory, Long> {
    boolean existsByCategoryId(String categoryId);
    HomeCategory findByCategoryId(String categoryId);


}
