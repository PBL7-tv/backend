package com.pbl7.product_service.repository.httpclient;

import com.pbl7.product_service.configuration.AuthenticationRequestInterceptor;
import com.pbl7.product_service.dto.CategoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "category-service", url = "${app.services.category}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface CategoryClient {
    @GetMapping("/internal/{id}")
    ResponseEntity<CategoryDTO> getCategory(@PathVariable("id") String id);

    @PostMapping("/api/categories")
    ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDTO);
}

