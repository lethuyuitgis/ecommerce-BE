package com.shopcuathuy.service;

import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductImage;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.Wishlist;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.repository.WishlistRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {
    
    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    public List<ProductDTO> getWishlist(String userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(wishlist -> toProductDTO(wishlist.getProduct()))
                .collect(Collectors.toList());
    }
    
    private ProductDTO toProductDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setComparePrice(product.getComparePrice());
        dto.setRating(product.getRating());
        dto.setTotalSold(product.getTotalSold());
        dto.setStatus(product.getStatus().name());
        
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        
        product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .ifPresent(img -> dto.setPrimaryImage(img.getImageUrl()));
        
        return dto;
    }
    
    public void addToWishlist(String userId, String productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            return; // Already in wishlist
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);
        wishlistRepository.save(wishlist);
    }
    
    public void removeFromWishlist(String userId, String productId) {
        Wishlist wishlist = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in wishlist"));
        
        wishlistRepository.delete(wishlist);
    }
    
    public boolean isInWishlist(String userId, String productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }
}

