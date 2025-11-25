package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.AddToCartRequestDTO;
import com.shopcuathuy.dto.request.UpdateCartItemRequestDTO;
import com.shopcuathuy.dto.response.CartItemResponseDTO;
import com.shopcuathuy.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemResponseDTO>>> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        
        List<CartItemResponseDTO> cartItems = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartItemResponseDTO>> addToCart(
            @RequestBody AddToCartRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        CartItemResponseDTO cartItem = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItemResponseDTO>> updateCartItem(
            @PathVariable String id,
            @RequestBody UpdateCartItemRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        CartItemResponseDTO cartItem = cartService.updateCartItem(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        cartService.removeFromCart(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
