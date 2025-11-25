package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateCategoryRequestDTO;
import com.shopcuathuy.dto.request.UpdateCategoryRequestDTO;
import com.shopcuathuy.dto.response.CategoryResponseDTO;
import com.shopcuathuy.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponseDTO>>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{idOrSlug}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> getCategory(@PathVariable String idOrSlug) {
        try {
            CategoryResponseDTO category = categoryService.getCategoryByIdOrSlug(idOrSlug);
            return ResponseEntity.ok(ApiResponse.success(category));
        } catch (com.shopcuathuy.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Category not found"));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> createCategory(
            @RequestBody CreateCategoryRequestDTO request) {
        CategoryResponseDTO category = categoryService.createCategory(request);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> updateCategory(
            @PathVariable String id,
            @RequestBody UpdateCategoryRequestDTO request) {
        CategoryResponseDTO category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (com.shopcuathuy.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Category not found"));
        }
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<CategoryResponseDTO>> toggleActive(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        CategoryResponseDTO category = categoryService.toggleActive(id, request.get("active"));
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}
