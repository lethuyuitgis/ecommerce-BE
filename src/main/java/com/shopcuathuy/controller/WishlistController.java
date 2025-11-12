package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.service.WishlistService;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getWishlist(@RequestHeader("X-User-Id") String userId) {
        List<ProductDTO> items = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Void>> addToWishlist(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> request) {
        String productId = request.get("productId");
        wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item added to wishlist", null));
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId) {
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from wishlist", null));
    }
    
    @GetMapping("/{productId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlist(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId) {
        boolean isInWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(isInWishlist));
    }
}

