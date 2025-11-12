package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.repository.CategoryRepository;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        try {
            List<Category> categories = categoryRepository.findByParentIsNullAndIsActiveTrue();
            log.info("Found {} categories", categories.size());
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            log.error("Error fetching categories", e);
            throw e;
        }
    }
    
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<Category>> getCategoryBySlug(@PathVariable String slug) {
        try {
            return categoryRepository.findBySlug(slug)
                    .map(category -> ResponseEntity.ok(ApiResponse.success(category)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching category by slug: {}", slug, e);
            throw e;
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<Category>> createCategory(@RequestBody CategoryRequest req) {
        Category category = new Category();
        category.setName(req.getName());
        category.setSlug(req.getSlug());
        category.setDescription(req.getDescription());
        category.setIcon(req.getIcon());
        category.setCoverImage(req.getCoverImage());
        category.setDisplayOrder(Optional.ofNullable(req.getDisplayOrder()).orElse(0));
        category.setIsActive(Optional.ofNullable(req.getIsActive()).orElse(true));
        
        if (req.getParentId() != null) {
            categoryRepository.findById(req.getParentId()).ifPresent(category::setParent);
        }
        category = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success("Category created", category));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> updateCategory(@PathVariable String id, @RequestBody CategoryRequest req) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        if (req.getName() != null) category.setName(req.getName());
        if (req.getSlug() != null) category.setSlug(req.getSlug());
        if (req.getDescription() != null) category.setDescription(req.getDescription());
        if (req.getIcon() != null) category.setIcon(req.getIcon());
        if (req.getCoverImage() != null) category.setCoverImage(req.getCoverImage());
        if (req.getDisplayOrder() != null) category.setDisplayOrder(req.getDisplayOrder());
        if (req.getIsActive() != null) category.setIsActive(req.getIsActive());
        if (req.getParentId() != null) {
            categoryRepository.findById(req.getParentId()).ifPresent(category::setParent);
        }
        category = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success("Category updated", category));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.ok(ApiResponse.error("Category not found"));
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
    
    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<Category>> toggleActive(@PathVariable String id, @RequestBody ToggleRequest body) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        category.setIsActive(Boolean.TRUE.equals(body.getActive()));
        category = categoryRepository.save(category);
        return ResponseEntity.ok(ApiResponse.success("Category status updated", category));
    }
    
    public static class CategoryRequest {
        private String name;
        private String slug;
        private String description;
        private String icon;
        private String coverImage;
        private String parentId;
        private Integer displayOrder;
        private Boolean isActive;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getCoverImage() { return coverImage; }
        public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
        public String getParentId() { return parentId; }
        public void setParentId(String parentId) { this.parentId = parentId; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
    
    public static class ToggleRequest {
        private Boolean active;
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }
}

