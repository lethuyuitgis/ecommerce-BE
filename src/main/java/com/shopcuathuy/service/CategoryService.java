package com.shopcuathuy.service;

import com.shopcuathuy.dto.request.CreateCategoryRequestDTO;
import com.shopcuathuy.dto.request.UpdateCategoryRequestDTO;
import com.shopcuathuy.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllCategories();
    CategoryResponseDTO getCategoryByIdOrSlug(String idOrSlug);
    CategoryResponseDTO createCategory(CreateCategoryRequestDTO request);
    CategoryResponseDTO updateCategory(String id, UpdateCategoryRequestDTO request);
    void deleteCategory(String id);
    CategoryResponseDTO toggleActive(String id, Boolean active);
}


