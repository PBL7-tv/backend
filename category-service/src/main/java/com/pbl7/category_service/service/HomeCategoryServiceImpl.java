package com.pbl7.category_service.service;


import com.pbl7.category_service.entity.HomeCategory;
import com.pbl7.category_service.repository.HomeCategoryRepository;
import com.pbl7.category_service.dto.request.CreateHomeCategoryRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeCategoryServiceImpl implements HomeCategoryService {

    HomeCategoryRepository homeCategoryRepository;

    @Override
    public HomeCategory createHomeCategory(HomeCategory homeCategory) {
        return homeCategoryRepository.save(homeCategory);
    }

    @Override
    public HomeCategory createHomeCategory(CreateHomeCategoryRequest request) {
        HomeCategory homeCategory = new HomeCategory();
        homeCategory.setName(request.getName());
        homeCategory.setImage(request.getImage());
        homeCategory.setCategoryId(request.getCategoryId());
        return homeCategoryRepository.save(homeCategory);
    }

    @Override
    public List<HomeCategory> createCategories(List<HomeCategory> homeCategories) {
        List<HomeCategory> categoriesToSave = new ArrayList<>();

        for (HomeCategory category : homeCategories) {
            if (!homeCategoryRepository.existsByCategoryId(category.getCategoryId().toLowerCase())) {
                categoriesToSave.add(category);
            }
        }

        homeCategoryRepository.saveAll(categoriesToSave);

        return homeCategoryRepository.findAll();
    }

    @Override
    public HomeCategory updateHomeCategory(HomeCategory homeCategory, Long id) throws Exception {
        HomeCategory existingHomeCategory = homeCategoryRepository.findById(id).orElseThrow(
                () -> new Exception("category not found")
        );

        if(homeCategory.getImage() != null && !homeCategory.getImage().isEmpty()){
            existingHomeCategory.setImage(homeCategory.getImage());
        }

        if(homeCategory.getCategoryId() != null && !homeCategory.getCategoryId().isEmpty()){
            existingHomeCategory.setCategoryId(homeCategory.getCategoryId());
        }
        if(homeCategory.getName() != null && !homeCategory.getName().isEmpty()){
            existingHomeCategory.setName(homeCategory.getName());
        }

        return homeCategoryRepository.save(existingHomeCategory);
    }

    @Override
    public List<HomeCategory> getAllHomeCategories() {
        return homeCategoryRepository.findAll();
    }

    @Override
    public HomeCategory getHomeCategoryById(Long id) throws Exception {
        return homeCategoryRepository.findById(id).orElseThrow(() -> new Exception("Category not found"));
    }
}
