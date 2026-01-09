package com.pbl7.category_service.controller;


import com.pbl7.category_service.entity.Home;
import com.pbl7.category_service.entity.HomeCategory;
import com.pbl7.category_service.service.HomeCategoryService;
import com.pbl7.category_service.service.HomeService;
import com.pbl7.category_service.dto.request.CreateHomeCategoryRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HomeCategoryController {

    HomeCategoryService homeCategoryService;
    HomeService homeService;


    @PostMapping("/home/categories")
    public ResponseEntity<Home> createHomeCategory(@RequestBody List<HomeCategory> homeCategoryList) {

        List<HomeCategory> categories = homeCategoryService.createCategories(homeCategoryList);
        Home home = homeService.createHomePageData(categories);

        return new ResponseEntity<>(home, HttpStatus.CREATED);
    }

    @GetMapping("/admin/home-category")
    public ResponseEntity<List<HomeCategory>> getHomeCategory() {

        List<HomeCategory> categories = homeCategoryService.getAllHomeCategories();

        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PatchMapping("/admin/home-category/{id}")
    public ResponseEntity<HomeCategory> updateHomeCategory(@PathVariable Long id, @RequestBody HomeCategory homeCategory, @RequestHeader("Authorization") String jwt) throws Exception {

        HomeCategory updateHomeCategory = homeCategoryService.updateHomeCategory(homeCategory, id);
        return new ResponseEntity<>(updateHomeCategory, HttpStatus.OK);
    }

    @PostMapping("/admin/home-category")
    public ResponseEntity<HomeCategory> createHomeCategory(@RequestBody CreateHomeCategoryRequest request) {
        HomeCategory homeCategory = homeCategoryService.createHomeCategory(request);
        return new ResponseEntity<>(homeCategory, HttpStatus.CREATED);
    }

    @GetMapping("/admin/home-category/{id}")
    public ResponseEntity<HomeCategory> getHomeCategoryById(@PathVariable Long id, @RequestHeader("Authorization") String jwt) throws Exception {
        HomeCategory homeCategory = homeCategoryService.getHomeCategoryById(id);
        return new ResponseEntity<>(homeCategory, HttpStatus.OK);
    }
//    @PatchMapping("/admin/home-category/{id}")
//    public ResponseEntity<HomeCategory> updateHomeCategory(@PathVariable Long id, @RequestBody HomeCategory homeCategory) throws Exception {
//
//        HomeCategory updateHomeCategory = homeCategoryService.updateHomeCategory(homeCategory, id);
//        return new ResponseEntity<>(updateHomeCategory, HttpStatus.OK);
//    }
}
