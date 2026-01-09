package com.pbl7.product_service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbl7.product_service.dto.request.CreateProductRequest;
import com.pbl7.product_service.dto.request.UpdateProductRequest;
import com.pbl7.product_service.dto.response.CreateProductResponse;
import com.pbl7.product_service.entity.Product;
import org.mapstruct.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ObjectMapper objectMapper = new ObjectMapper();

    @Mapping(target = "specs", source = "specs", qualifiedByName = "mapToJson")
    Product toProduct(CreateProductRequest request);

    @Mapping(target = "title", source = "title")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "sellingPrice", source = "sellingPrice")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "specs", source = "specs", qualifiedByName = "mapToJson")
    void updateProductFromRequest(UpdateProductRequest request, @MappingTarget Product product);

    @Mapping(target = "specs", source = "specs", qualifiedByName = "jsonToMap")
    CreateProductResponse toCreateProductResponse(Product product);

    @Named("mapToJson")
    static String mapToJson(Map<String, Object> map) {
        if (map == null) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Named("jsonToMap")
    static Map<String, Object> jsonToMap(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, HashMap.class);
        } catch (IOException e) {
            return null;
        }
    }
}