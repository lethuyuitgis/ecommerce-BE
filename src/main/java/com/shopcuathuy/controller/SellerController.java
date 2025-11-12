package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.dto.SellerDTO;
import com.shopcuathuy.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SellerController {
    
    private final SellerService sellerService;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerDTO>> getProfile(@RequestHeader("X-User-Id") String userId) {
        SellerDTO seller = sellerService.getSellerProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(seller));
    }
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<SellerDTO>> createSeller(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SellerService.CreateSellerRequest request) {
        SellerDTO seller = sellerService.createSeller(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Seller profile created", seller));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<SellerDTO>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SellerService.UpdateSellerRequest request) {
        SellerDTO seller = sellerService.updateSellerProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", seller));
    }
    
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProducts(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(sellerService.getSellerProducts(userId, pageable)));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SellerService.CreateProductRequest request
    ) {
        ProductDTO product = sellerService.createProduct(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Product created", product));
    }
    
    @PutMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId,
            @RequestBody SellerService.UpdateProductRequest request
    ) {
        ProductDTO product = sellerService.updateProduct(userId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", product));
    }
}




