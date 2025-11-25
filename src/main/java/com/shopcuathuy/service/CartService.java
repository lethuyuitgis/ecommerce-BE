package com.shopcuathuy.service;

import com.shopcuathuy.dto.request.AddToCartRequestDTO;
import com.shopcuathuy.dto.request.UpdateCartItemRequestDTO;
import com.shopcuathuy.dto.response.CartItemResponseDTO;
import java.util.List;


public interface CartService {
    List<CartItemResponseDTO> getCart(String userId);
    CartItemResponseDTO addToCart(String userId, AddToCartRequestDTO request);
    CartItemResponseDTO updateCartItem(String userId, String cartItemId, UpdateCartItemRequestDTO request);
    void removeFromCart(String userId, String cartItemId);
    void clearCart(String userId);
}
