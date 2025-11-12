package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.CartItemDTO;
import com.shopcuathuy.service.CartService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.service.WishlistService;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {
    
    private final CartService cartService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCartItems(@RequestHeader("X-User-Id") String userId) {
        List<CartItemDTO> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
    
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, Object> request) {
        String productId = (String) request.get("productId");
        String variantId = (String) request.get("variantId");
        Integer quantity = ((Number) request.getOrDefault("quantity", 1)).intValue();
        
        CartItemDTO item = cartService.addToCart(userId, productId, variantId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", item));
    }
    
    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String cartItemId,
            @RequestBody Map<String, Integer> request) {
        Integer quantity = request.get("quantity");
        CartItemDTO item = cartService.updateCartItem(userId, cartItemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", item));
    }
    
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String cartItemId) {
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", null));
    }
    
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}

