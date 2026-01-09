package com.pbl7.category_service.controller;

import com.pbl7.category_service.dto.CategoryDTO;
import com.pbl7.category_service.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pbl7.category_service.dto.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryInternalController {
    CategoryService categoryService;

    @GetMapping("/{id}")
    ResponseEntity<CategoryDTO> getCategory(@PathVariable String id) {
        CategoryDTO categoryDTO = categoryService.getCategoryById(id);
        return ResponseEntity.ok(categoryDTO);
    }

}
