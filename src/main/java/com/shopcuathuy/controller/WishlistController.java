package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.AddToWishlistRequestDTO;
import com.shopcuathuy.dto.response.WishlistItemResponseDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.Wishlist;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.exception.UnauthorizedException;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public WishlistController(WishlistRepository wishlistRepository,
                              ProductRepository productRepository,
                              UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemResponseDTO>>> getWishlist(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            throw new UnauthorizedException("User not authenticated");
        }

        List<Wishlist> wishlistItems = wishlistRepository.findByUserId(userId);
        List<WishlistItemResponseDTO> items = wishlistItems.stream()
            .map(wishlist -> {
                Product product = wishlist.getProduct();
                if (product == null) return null;
                
                WishlistItemResponseDTO item = new WishlistItemResponseDTO();
                item.productId = product.getId();
                item.productName = product.getName();
                item.productPrice = product.getPrice() != null ? product.getPrice().doubleValue() : null;
                item.productImage = product.getImages() != null && !product.getImages().isEmpty() ?
                    product.getImages().get(0).getImageUrl() : null;
                item.addedAt = wishlist.getCreatedAt() != null ? 
                    wishlist.getCreatedAt().toString() : 
                    java.time.Instant.now().toString();
                return item;
            })
            .filter(item -> item != null)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<WishlistItemResponseDTO>> addToWishlist(
            @RequestBody AddToWishlistRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            throw new UnauthorizedException("User not authenticated");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(request.productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if already in wishlist
        if (wishlistRepository.existsByUserIdAndProductId(userId, request.productId)) {
            Wishlist existing = wishlistRepository.findByUserIdAndProductId(userId, request.productId)
                .orElse(null);
            if (existing != null) {
                WishlistItemResponseDTO item = convertToDTO(existing);
                return ResponseEntity.ok(ApiResponse.success(item));
            }
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setId(UUID.randomUUID().toString());
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlist = wishlistRepository.save(wishlist);

        WishlistItemResponseDTO item = convertToDTO(wishlist);
        return ResponseEntity.ok(ApiResponse.success(item));
    }

    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable String productId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            throw new UnauthorizedException("User not authenticated");
        }

        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
            .orElse(null);
        
        if (wishlist == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Product not in wishlist"));
        }

        wishlistRepository.delete(wishlist);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> checkInWishlist(
            @PathVariable String productId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }

        boolean inWishlist = wishlistRepository.existsByUserIdAndProductId(userId, productId);
        return ResponseEntity.ok(ApiResponse.success(inWishlist));
    }

    private WishlistItemResponseDTO convertToDTO(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        if (product == null) return null;
        
        WishlistItemResponseDTO item = new WishlistItemResponseDTO();
        item.productId = product.getId();
        item.productName = product.getName();
        item.productPrice = product.getPrice() != null ? product.getPrice().doubleValue() : null;
        item.productImage = product.getImages() != null && !product.getImages().isEmpty() ?
            product.getImages().get(0).getImageUrl() : null;
        item.addedAt = wishlist.getCreatedAt() != null ? 
            wishlist.getCreatedAt().toString() : 
            java.time.Instant.now().toString();
        return item;
    }
}
