package com.shopcuathuy.service;

import com.shopcuathuy.dto.CartItemDTO;
import com.shopcuathuy.entity.CartItem;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductVariant;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.CartItemRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.ProductVariantRepository;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    
    public List<CartItemDTO> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public CartItemDTO addToCart(String userId, String productId, String variantId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (product.getStatus() != Product.ProductStatus.ACTIVE) {
            throw new BadRequestException("Product is not available");
        }
        
        ProductVariant variant = null;
        if (variantId != null) {
            variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
        }
        
        // Check if item already exists in cart
        CartItem existingItem = variantId != null
                ? cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, productId, variantId).orElse(null)
                : cartItemRepository.findByUserIdAndProductId(userId, productId).orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
            return toDTO(existingItem);
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setUser(new com.shopcuathuy.entity.User());
        cartItem.getUser().setId(userId);
        cartItem.setProduct(product);
        cartItem.setVariant(variant);
        cartItem.setQuantity(quantity);
        
        cartItem = cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }
    
    public CartItemDTO updateCartItem(String userId, String cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }
        
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }
        
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }
    
    public void removeFromCart(String userId, String cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new BadRequestException("Unauthorized");
        }
        
        cartItemRepository.delete(cartItem);
    }
    
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
    
    private CartItemDTO toDTO(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setAvailableQuantity(cartItem.getProduct().getQuantity());
        
        // Get primary image
        cartItem.getProduct().getImages().stream()
                .filter(img -> img.getIsPrimary())
                .findFirst()
                .ifPresent(img -> dto.setProductImage(img.getImageUrl()));
        
        if (cartItem.getVariant() != null) {
            dto.setVariantId(cartItem.getVariant().getId());
            dto.setVariantName(cartItem.getVariant().getVariantName());
            dto.setVariantPrice(cartItem.getVariant().getVariantPrice());
            dto.setAvailableQuantity(cartItem.getVariant().getVariantQuantity());
        }
        
        return dto;
    }
}

