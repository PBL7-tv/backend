package com.pbl7.category_service.service;


import com.pbl7.category_service.dto.request.CreateHomeCategoryRequest;
import com.pbl7.category_service.entity.HomeCategory;

import java.util.List;

public interface HomeCategoryService {
    HomeCategory createHomeCategory(HomeCategory homeCategory);
    List<HomeCategory> createCategories(List<HomeCategory> homeCategories);
    HomeCategory updateHomeCategory(HomeCategory homeCategory, Long id) throws Exception;
    List<HomeCategory> getAllHomeCategories();
    HomeCategory createHomeCategory(CreateHomeCategoryRequest homeCategory);
    HomeCategory getHomeCategoryById(Long id) throws Exception;
}
