package com.shopcuathuy.service;

import com.shopcuathuy.dto.ProductReviewDTO;
import com.shopcuathuy.entity.OrderItem;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductReview;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderItemRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.ProductReviewRepository;
import com.shopcuathuy.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    
    public Page<ProductReviewDTO> getProductReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndStatus(productId, ProductReview.ReviewStatus.APPROVED, pageable)
                .map(this::toDTO);
    }
    
    public ProductReviewDTO createReview(String userId, String productId, CreateReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Verify user purchased the product
        if (request.getOrderItemId() != null) {
            OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
            
            if (!orderItem.getOrder().getCustomer().getId().equals(userId)) {
                throw new BadRequestException("Unauthorized");
            }
        }
        
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setStatus(ProductReview.ReviewStatus.PENDING);
        
        if (request.getOrderItemId() != null) {
            OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId()).orElse(null);
            review.setOrderItem(orderItem);
        }
        
        review = reviewRepository.save(review);
        
        // Update product rating (simplified - should recalculate from all reviews)
        updateProductRating(product);
        
        return toDTO(review);
    }
    
    private void updateProductRating(Product product) {
        List<ProductReview> approvedReviews = reviewRepository.findByProductIdAndStatus(
                product.getId(), ProductReview.ReviewStatus.APPROVED, 
                org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();
        
        if (!approvedReviews.isEmpty()) {
            BigDecimal avgRating = approvedReviews.stream()
                    .map(r -> BigDecimal.valueOf(r.getRating()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(approvedReviews.size()), 2, RoundingMode.HALF_UP);
            
            product.setRating(avgRating);
            product.setTotalReviews(approvedReviews.size());
            productRepository.save(product);
        }
    }
    
    private ProductReviewDTO toDTO(ProductReview review) {
        ProductReviewDTO dto = new ProductReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getFullName());
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
    
    public static class CreateReviewRequest {
        private String orderItemId;
        private Integer rating;
        private String title;
        private String comment;
        
        // Getters and setters
        public String getOrderItemId() { return orderItemId; }
        public void setOrderItemId(String orderItemId) { this.orderItemId = orderItemId; }
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}

