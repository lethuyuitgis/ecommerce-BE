package com.shopcuathuy.service;

import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductImage;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toDTO);
    }
    
    public Page<ProductDTO> getProductsByCategory(String categorySlug, Pageable pageable) {
        return productRepository.findByCategorySlug(categorySlug, pageable)
                .map(this::toDTO);
    }
    
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(keyword, pageable)
                .map(this::toDTO);
    }
    
    public ProductDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDTO(product);
    }
    
    public Page<ProductDTO> getFeaturedProducts(Pageable pageable) {
        return productRepository.findFeaturedProducts(Product.ProductStatus.ACTIVE, pageable)
                .map(this::toDTO);
    }
    
    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setComparePrice(product.getComparePrice());
        dto.setQuantity(product.getQuantity());
        dto.setMinOrder(product.getMinOrder());
        dto.setStatus(product.getStatus().name());
        dto.setRating(product.getRating());
        dto.setTotalReviews(product.getTotalReviews());
        dto.setTotalSold(product.getTotalSold());
        dto.setTotalViews(product.getTotalViews());
        dto.setIsFeatured(product.getIsFeatured());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getSeller() != null) {
            dto.setSellerId(product.getSeller().getId());
            dto.setSellerName(product.getSeller().getShopName());
        }
        
        List<String> images = product.getImages().stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
        dto.setImages(images);
        
        product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .ifPresent(img -> dto.setPrimaryImage(img.getImageUrl()));
        
        return dto;
    }
}
