package com.pbl7.category_service.service;

import com.pbl7.category_service.dto.CategoryDTO;

public interface CategoryService {
    CategoryDTO getCategoryById(String id);
}
