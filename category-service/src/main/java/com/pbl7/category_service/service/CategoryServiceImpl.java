package com.pbl7.category_service.service;

import com.pbl7.category_service.dto.CategoryDTO;
import com.pbl7.category_service.entity.Category;
import com.pbl7.category_service.entity.HomeCategory;
import com.pbl7.category_service.exception.AppException;
import com.pbl7.category_service.exception.ErrorCode;
import com.pbl7.category_service.repository.CategoryRepository;
import com.pbl7.category_service.repository.HomeCategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {
    HomeCategoryRepository homeCategoryRepository;
    CategoryRepository categoryRepository;

    @Override
    public CategoryDTO getCategoryById(String id) {
        HomeCategory homeCategory = homeCategoryRepository.findByCategoryId(id);
        if (homeCategory == null) {
            throw new AppException(ErrorCode.CATEGORY_404);
        }

        return CategoryDTO.builder()
                .categoryId(homeCategory.getCategoryId())
                .build();
    }

}
