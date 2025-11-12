package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.entity.Category;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.repository.CategoryRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.service.ShopeeCrawlerService;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để quản lý crawler và xem data đã crawl
 */
@RestController
@RequestMapping("/api/admin/crawler")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CrawlerController {
    
    private final ShopeeCrawlerService shopeeCrawlerService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    /**
     * Xem thống kê data đã crawl
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalCategories = categoryRepository.count();
        long totalProducts = productRepository.count();
        long activeCategories = categoryRepository.count();
        
        // Count active products
        long activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getStatus() == Product.ProductStatus.ACTIVE)
                .count();
        
        stats.put("totalCategories", totalCategories);
        stats.put("totalProducts", totalProducts);
        stats.put("activeCategories", activeCategories);
        stats.put("activeProducts", activeProducts);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * Xem danh sách categories đã crawl
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<Page<Category>>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categories = categoryRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
    
    /**
     * Xem danh sách products đã crawl
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<Product>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Xem products theo category
     */
    @GetMapping("/products/category/{categorySlug}")
    public ResponseEntity<ApiResponse<Page<Product>>> getProductsByCategory(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findByCategorySlug(categorySlug, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Crawl categories từ Shopee
     */
    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<String>> crawlCategories() {
        try {
            shopeeCrawlerService.crawlCategories();
            return ResponseEntity.ok(ApiResponse.success("Categories crawling completed"));
        } catch (Exception e) {
            log.error("Error crawling categories", e);
            return ResponseEntity.ok(ApiResponse.error("Error crawling categories: " + e.getMessage()));
        }
    }
    
    /**
     * Crawl products từ Shopee theo category
     */
    @PostMapping("/products/{categorySlug}")
    public ResponseEntity<ApiResponse<String>> crawlProducts(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            shopeeCrawlerService.crawlProducts(categorySlug, limit);
            return ResponseEntity.ok(ApiResponse.success("Products crawling completed for category: " + categorySlug));
        } catch (Exception e) {
            log.error("Error crawling products", e);
            return ResponseEntity.ok(ApiResponse.error("Error crawling products: " + e.getMessage()));
        }
    }
}

