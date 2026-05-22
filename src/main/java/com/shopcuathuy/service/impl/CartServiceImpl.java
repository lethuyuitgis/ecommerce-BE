package com.shopcuathuy.service.impl;

import com.shopcuathuy.dto.request.AddToCartRequestDTO;
import com.shopcuathuy.dto.request.UpdateCartItemRequestDTO;
import com.shopcuathuy.dto.response.CartItemResponseDTO;
import com.shopcuathuy.entity.CartItem;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.CartItemRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.ProductVariantRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Autowired
    public CartServiceImpl(CartItemRepository cartItemRepository, 
                          ProductRepository productRepository,
                          UserRepository userRepository,
                          ProductVariantRepository productVariantRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public List<CartItemResponseDTO> getCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CartItemResponseDTO addToCart(String userId, AddToCartRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(request.productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        com.shopcuathuy.entity.ProductVariant variant = null;
        if (request.variantId != null && !request.variantId.isEmpty()) {
            variant = productVariantRepository.findById(request.variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        }

        // Check if item already exists
        Optional<CartItem> existingItem;
        if (variant != null) {
            existingItem = cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, request.productId, request.variantId);
        } else {
            existingItem = cartItemRepository.findByUserIdAndProductId(userId, request.productId);
        }

        CartItem cartItem;
        Integer quantityToAdd = request.quantity != null ? request.quantity : 1;
        Integer newTotalQuantity;
        
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            newTotalQuantity = cartItem.getQuantity() + quantityToAdd;
        } else {
            cartItem = new CartItem();
            cartItem.setId(UUID.randomUUID().toString());
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setVariant(variant);
            newTotalQuantity = quantityToAdd;
        }

        // Stock Validation
        if (variant != null) {
            if (variant.getVariantQuantity() < newTotalQuantity) {
                throw new IllegalStateException("Số lượng trong kho không đủ (Còn " + variant.getVariantQuantity() + " sản phẩm)");
            }
        } else {
            if (product.getQuantity() < newTotalQuantity) {
                throw new IllegalStateException("Số lượng trong kho không đủ (Còn " + product.getQuantity() + " sản phẩm)");
            }
        }

        cartItem.setQuantity(newTotalQuantity);
        cartItem = cartItemRepository.save(cartItem);
        return convertToDTO(cartItem);
    }

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItem(String userId, String cartItemId, UpdateCartItemRequestDTO request) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        if (request.quantity == null || request.quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        // Stock Validation
        if (cartItem.getVariant() != null) {
            if (cartItem.getVariant().getVariantQuantity() < request.quantity) {
                throw new IllegalStateException("Số lượng trong kho không đủ (Còn " + cartItem.getVariant().getVariantQuantity() + " sản phẩm)");
            }
        } else if (cartItem.getProduct() != null) {
            if (cartItem.getProduct().getQuantity() < request.quantity) {
                throw new IllegalStateException("Số lượng trong kho không đủ (Còn " + cartItem.getProduct().getQuantity() + " sản phẩm)");
            }
        }

        cartItem.setQuantity(request.quantity);
        cartItem = cartItemRepository.save(cartItem);
        return convertToDTO(cartItem);
    }

    @Override
    @Transactional
    public void removeFromCart(String userId, String cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        if (!cartItem.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public CartItemResponseDTO convertToDTO(CartItem cartItem) {
        CartItemResponseDTO dto = new CartItemResponseDTO();
        dto.id = cartItem.getId();
        dto.productId = cartItem.getProduct() != null ? cartItem.getProduct().getId() : null;
        dto.productName = cartItem.getProduct() != null ? cartItem.getProduct().getName() : null;
        dto.productPrice = cartItem.getProduct() != null && cartItem.getProduct().getPrice() != null 
            ? cartItem.getProduct().getPrice().doubleValue() : null;
        dto.productImage = cartItem.getProduct() != null && !cartItem.getProduct().getImages().isEmpty()
            ? cartItem.getProduct().getImages().get(0).getImageUrl() : null;
        dto.variantId = cartItem.getVariant() != null ? cartItem.getVariant().getId() : null;
        dto.variantName = cartItem.getVariant() != null ? cartItem.getVariant().getVariantName() : null;
        dto.variantPrice = cartItem.getVariant() != null && cartItem.getVariant().getVariantPrice() != null
            ? cartItem.getVariant().getVariantPrice().doubleValue() : null;
        dto.quantity = cartItem.getQuantity();
        dto.availableQuantity = cartItem.getProduct() != null ? cartItem.getProduct().getQuantity() : null;
        return dto;
    }
}


