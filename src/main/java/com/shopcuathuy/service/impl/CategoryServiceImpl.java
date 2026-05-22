package com.shopcuathuy.service.impl;

import com.shopcuathuy.dto.request.CreateCategoryRequestDTO;
import com.shopcuathuy.dto.request.UpdateCategoryRequestDTO;
import com.shopcuathuy.dto.response.CategoryResponseDTO;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.CategoryRepository;
import com.shopcuathuy.service.CategoryService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Cacheable(value = "categories:all")
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> allCategories = categoryRepository.findByParentIsNullAndIsActiveTrue();
        return allCategories.stream()
            .map(this::convertToDTO)
            .sorted((a, b) -> {
                int orderCompare = Integer.compare(
                    a.displayOrder != null ? a.displayOrder : 0,
                    b.displayOrder != null ? b.displayOrder : 0
                );
                if (orderCompare != 0) return orderCompare;
                return a.name.compareTo(b.name);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categories:single", key = "#idOrSlug")
    public CategoryResponseDTO getCategoryByIdOrSlug(String idOrSlug) {
        Category category = categoryRepository.findById(idOrSlug).orElse(null);
        
        if (category == null) {
            category = categoryRepository.findBySlug(idOrSlug).orElse(null);
        }
        
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }
        
        return convertToDTO(category);
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
            .replaceAll("[èéẹẻẽêềếệểễ]", "e")
            .replaceAll("[ìíịỉĩ]", "i")
            .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
            .replaceAll("[ùúụủũưừứựửữ]", "u")
            .replaceAll("[ỳýỵỷỹ]", "y")
            .replaceAll("đ", "d")
            .replaceAll("\\s+", "-")
            .replaceAll("[^a-z0-9-]", "");
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories:all", "categories:single"}, allEntries = true)
    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO request) {
        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setName(request.name);
        category.setSlug(request.slug != null && !request.slug.isEmpty() ? request.slug : generateSlug(request.name));
        category.setDescription(request.description);
        category.setIcon(request.icon);
        category.setCoverImage(request.coverImage);
        category.setDisplayOrder(request.displayOrder != null ? request.displayOrder : 0);
        category.setIsActive(request.isActive != null ? request.isActive : true);
        
        if (request.parentId != null) {
            Category parent = categoryRepository.findById(request.parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }
        
        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories:all", "categories:single"}, allEntries = true)
    public CategoryResponseDTO updateCategory(String id, UpdateCategoryRequestDTO request) {
        Category existing = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.name != null) {
            existing.setName(request.name);
            // Auto update slug if name changes and NO manual slug provided
            if (request.slug == null || request.slug.isEmpty()) {
                existing.setSlug(generateSlug(request.name));
            }
        }
        if (request.description != null) existing.setDescription(request.description);
        if (request.slug != null && !request.slug.isEmpty()) existing.setSlug(request.slug);
        if (request.icon != null) existing.setIcon(request.icon);
        if (request.coverImage != null) existing.setCoverImage(request.coverImage);
        if (request.parentId != null) {
            Category parent = categoryRepository.findById(request.parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            existing.setParent(parent);
        }
        if (request.displayOrder != null) existing.setDisplayOrder(request.displayOrder);
        if (request.isActive != null) existing.setIsActive(request.isActive);

        existing = categoryRepository.save(existing);
        return convertToDTO(existing);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories:all", "categories:single"}, allEntries = true)
    public void deleteCategory(String id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"categories:all", "categories:single"}, allEntries = true)
    public CategoryResponseDTO toggleActive(String id, Boolean active) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (active != null) {
            category.setIsActive(active);
        } else {
            category.setIsActive(!category.getIsActive());
        }

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    public CategoryResponseDTO convertToDTO(Category category) {
        return convertToDTO(category, true);
    }

    private CategoryResponseDTO convertToDTO(Category category, boolean includeChildren) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.id = category.getId();
        dto.name = category.getName();
        dto.slug = category.getSlug();
        dto.description = category.getDescription();
        dto.icon = category.getIcon();
        dto.coverImage = category.getCoverImage();
        dto.parentId = category.getParent() != null ? category.getParent().getId() : null;
        dto.displayOrder = category.getDisplayOrder();
        dto.isActive = category.getIsActive();
        
        // Calculate product count (more efficient for heavy trees)
        dto.productCount = calculateRecursiveProductCount(category);
        
        // Generate full path
        StringBuilder path = new StringBuilder(category.getName());
        Category current = category.getParent();
        while (current != null) {
            path.insert(0, current.getName() + " > ");
            current = current.getParent();
        }
        dto.fullPath = path.toString();

        if (includeChildren && category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.children = category.getChildren().stream()
                .filter(Category::getIsActive)
                .map(c -> convertToDTO(c, false)) // Only one level deep for bulk listings
                .collect(Collectors.toList());
            
            dto.subcategories = category.getChildren().stream()
                .filter(Category::getIsActive)
                .map(Category::getName)
                .collect(Collectors.toList());
        }
        
        return dto;
    }

    private int calculateRecursiveProductCount(Category category) {
        int count = category.getProducts() != null ? category.getProducts().size() : 0;
        if (category.getChildren() != null) {
            for (Category child : category.getChildren()) {
                count += calculateRecursiveProductCount(child);
            }
        }
        return count;
    }
}


