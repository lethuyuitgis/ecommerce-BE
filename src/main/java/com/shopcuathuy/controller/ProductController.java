package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.UpdateProductRequestDTO;
import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;
import com.shopcuathuy.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProductPageResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        ProductPageResponseDTO result = productService.getAllProducts(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<ProductPageResponseDTO>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductPageResponseDTO result = productService.getFeaturedProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/flash-sales")
    public ResponseEntity<ApiResponse<ProductPageResponseDTO>> getFlashSaleProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductPageResponseDTO result = productService.getFlashSaleProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable String id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/category/{slug}")
    public ResponseEntity<ApiResponse<ProductPageResponseDTO>> getProductsByCategory(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String subcategory) {
        ProductPageResponseDTO result = productService.getProductsByCategorySlug(
            slug, page, size, minPrice, maxPrice, minRating, subcategory);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<ProductPageResponseDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        ProductPageResponseDTO result = productService.searchProducts(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable String id,
            @RequestBody UpdateProductRequestDTO request) {
        ProductResponseDTO updated = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStats(
            @PathVariable String id,
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> stats = productService.getProductStats(id, days);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
