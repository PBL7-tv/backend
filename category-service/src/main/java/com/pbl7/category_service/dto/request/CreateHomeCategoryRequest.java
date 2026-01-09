package com.pbl7.category_service.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHomeCategoryRequest {
    private String name;
    private String image;
    private String categoryId;
} 