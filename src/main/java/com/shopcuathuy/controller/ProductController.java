package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.UpdateProductRequestDTO;
import com.shopcuathuy.dto.response.ProductPageResponseDTO;
import com.shopcuathuy.dto.response.ProductResponseDTO;
import com.shopcuathuy.service.ProductService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        ProductPageResponseDTO result = productService.searchProducts(keyword, page, size);
        
        // Apply additional filters if provided
        if (categoryId != null || minPrice != null || maxPrice != null || minRating != null) {
            List<ProductResponseDTO> filtered = result.content.stream()
                .filter(p -> {
                    if (categoryId != null && !p.categoryId.equals(categoryId)) return false;
                    if (minPrice != null && (p.price == null || p.price < minPrice)) return false;
                    if (maxPrice != null && (p.price == null || p.price > maxPrice)) return false;
                    if (minRating != null && (p.rating == null || p.rating < minRating)) return false;
                    return true;
                })
                .collect(Collectors.toList());
            
            // Apply sorting
            Comparator<ProductResponseDTO> comparator = getComparator(sortBy, direction);
            filtered.sort(comparator);
            
            // Re-apply pagination after filtering
            int start = page * size;
            int end = Math.min(start + size, filtered.size());
            List<ProductResponseDTO> paginated = start < filtered.size() 
                ? filtered.subList(start, end)
                : new ArrayList<>();
            
            result = new ProductPageResponseDTO(
                paginated,
                filtered.size(),
                (int) Math.ceil((double) filtered.size() / size),
                size,
                page
            );
        } else {
            // Apply sorting if no filters
            Comparator<ProductResponseDTO> comparator = getComparator(sortBy, direction);
            result.content.sort(comparator);
        }
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    private Comparator<ProductResponseDTO> getComparator(String sortBy, String direction) {
        Comparator<ProductResponseDTO> comparator;
        switch (sortBy.toLowerCase()) {
            case "price":
                comparator = Comparator.comparing(p -> p.price != null ? p.price : 0.0);
                break;
            case "rating":
                comparator = Comparator.comparing(p -> p.rating != null ? p.rating : 0.0);
                break;
            case "sold":
                comparator = Comparator.comparing(p -> p.totalSold != null ? p.totalSold : 0);
                break;
            case "name":
                comparator = Comparator.comparing(p -> p.name != null ? p.name : "");
                break;
            default:
                comparator = Comparator.comparing(p -> p.createdAt != null ? p.createdAt : Instant.MIN);
        }
        
        return "ASC".equalsIgnoreCase(direction) ? comparator : comparator.reversed();
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
