package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.UpdateFeaturedRequestDTO;
import com.shopcuathuy.dto.request.UpdateFlashSaleRequestDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.service.ProductService;
import com.shopcuathuy.service.impl.ProductServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductAdminController {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ProductServiceImpl productServiceImpl;

    @Autowired
    public ProductAdminController(ProductRepository productRepository, ProductService productService, ProductServiceImpl productServiceImpl) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.productServiceImpl = productServiceImpl;
    }

    @PostMapping("/{id}/featured")
    @Transactional
    @CacheEvict(value = {"products:featured"}, allEntries = true)
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateFeatured(
            @PathVariable String id,
            @RequestBody UpdateFeaturedRequestDTO request
    ) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean enabled = request.featured != null ? request.featured : true;
        product.setIsFeatured(enabled);
        // Note: featuredPriority is not in Product entity, may need to add if needed

        product = productRepository.save(product);
        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }

    @PostMapping("/{id}/flash-sale")
    @Transactional
    @CacheEvict(value = {"products:flash-sale"}, allEntries = true)
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateFlashSale(
            @PathVariable String id,
            @RequestBody UpdateFlashSaleRequestDTO request
    ) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        boolean enabled = request.enabled != null ? request.enabled : true;
        if (enabled) {
            if (request.flashPrice == null || request.flashPrice <= 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("flashPrice is required when enabling flash sale"));
            }
            // Note: Flash sale fields may not be in Product entity
            // You may need to add these fields or use a separate FlashSale entity
            // For now, we'll just update the price temporarily
            product.setPrice(BigDecimal.valueOf(request.flashPrice));
        } else {
            // Reset to original price if needed
            // This is a simplified implementation
        }

        product = productRepository.save(product);
        return ResponseEntity.ok(ApiResponse.success(productServiceImpl.convertToDTO(product)));
    }

    private Instant parseInstantOrNow(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            return Instant.now();
        }
    }

    private Instant parseInstantOrDefaultEnd(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now().plusSeconds(6 * 3600);
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ex) {
            return Instant.now().plusSeconds(6 * 3600);
        }
    }

}

