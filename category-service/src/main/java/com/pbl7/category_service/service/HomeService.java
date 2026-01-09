package com.pbl7.category_service.service;


import com.pbl7.category_service.entity.Home;
import com.pbl7.category_service.entity.HomeCategory;

import java.util.List;

public interface HomeService {
    Home createHomePageData(List<HomeCategory> allCategories);

}
