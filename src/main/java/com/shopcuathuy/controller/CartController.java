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
        
        // Allow guest users to add to cart (will be stored in localStorage on frontend)
        // When user logs in, frontend will sync cart to server
        if (userId == null || userId.isEmpty()) {
            // Return success but indicate it's a guest cart
            // Frontend will handle storing in localStorage
            return ResponseEntity.ok(ApiResponse.success("Item added to guest cart. Please login to save permanently.", (CartItemResponseDTO) null));
        }

        CartItemResponseDTO cartItem = cartService.addToCart(userId, request);
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CartItemResponseDTO>> updateCartItem(
            @PathVariable String id,
            @RequestBody UpdateCartItemRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        // Allow guest users to update cart (handled in localStorage on frontend)
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Cart updated in guest mode. Please login to save permanently.", (CartItemResponseDTO) null));
        }

        CartItemResponseDTO cartItem = cartService.updateCartItem(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(cartItem));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        // Allow guest users to remove from cart (handled in localStorage on frontend)
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Item removed from guest cart.", (Void) null));
        }

        cartService.removeFromCart(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        // Allow guest users to clear cart (handled in localStorage on frontend)
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Guest cart cleared.", (Void) null));
        }

        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
