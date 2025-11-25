package com.shopcuathuy.service;

import com.shopcuathuy.admin.dto.PageResponse;
import com.shopcuathuy.dto.ReviewDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.ProductReview;
import com.shopcuathuy.entity.ReviewImage;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.ProductReviewRepository;
import com.shopcuathuy.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ProductReviewRepository reviewRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<ReviewDTO> getProductReviews(String productId, int page, int size) {
        int effectiveSize = size <= 0 ? 20 : size;
        Pageable pageable = PageRequest.of(page, effectiveSize);
        
        Page<ProductReview> reviewPage = reviewRepository.findByProductIdAndStatus(
            productId, 
            ProductReview.ReviewStatus.APPROVED, 
            pageable
        );

        List<ReviewDTO> content = reviewPage.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content, 
            (int) reviewPage.getTotalElements(), 
            reviewPage.getTotalPages(), 
            effectiveSize, 
            page
        );
    }

    @Transactional
    public ReviewDTO createReview(
            String userId,
            String productId,
            Integer rating,
            String title,
            String comment,
            String orderItemId,
            List<MultipartFile> images,
            List<MultipartFile> videos
    ) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        User user = userId != null ? 
            userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")) :
            null;

        ProductReview review = new ProductReview();
        review.setId(UUID.randomUUID().toString());
        review.setProduct(product);
        review.setUser(user != null ? user : null);
        review.setRating(rating);
        review.setTitle(title);
        review.setComment(comment);
        review.setHelpfulCount(0);
        review.setStatus(ProductReview.ReviewStatus.PENDING); // Will be approved by admin

        // Save review first
        review = reviewRepository.save(review);

        // Handle images - create ReviewImage entities
        List<String> imageUrls = extractFileUrls(images, "review-images");
        for (String imageUrl : imageUrls) {
            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setId(UUID.randomUUID().toString());
            reviewImage.setReview(review);
            reviewImage.setImageUrl(imageUrl);
            review.getReviewImages().add(reviewImage);
        }

        // Note: Videos are not stored in ProductReview entity currently
        // You may need to add a ReviewVideo entity if needed

        review = reviewRepository.save(review);
        return toDTO(review);
    }

    @Transactional
    public void markHelpful(String userId, String reviewId) {
        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    private List<String> extractFileUrls(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String safeName = file.getOriginalFilename() != null
                        ? file.getOriginalFilename().replaceAll("\\s+", "-")
                        : UUID.randomUUID().toString();
                urls.add("/uploads/" + folder + "/" + safeName);
            }
        }
        return urls;
    }

    private ReviewDTO toDTO(ProductReview review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
        dto.setUserId(review.getUser() != null ? review.getUser().getId() : null);
        dto.setUserName(review.getUser() != null ? review.getUser().getFullName() : "Anonymous");
        dto.setUserAvatar(review.getUser() != null ? review.getUser().getAvatarUrl() : null);
        dto.setRating(review.getRating());
        dto.setTitle(review.getTitle());
        dto.setComment(review.getComment());
        
        // Extract images from ReviewImage entities
        List<String> imageUrls = review.getReviewImages() != null ?
            review.getReviewImages().stream()
                .map(ReviewImage::getImageUrl)
                .collect(Collectors.toList()) :
            new ArrayList<>();
        dto.setImages(imageUrls);
        
        // Note: Videos are not currently stored in ProductReview entity
        dto.setVideos(new ArrayList<>());
        
        dto.setHelpfulCount(review.getHelpfulCount());
        dto.setCreatedAt(review.getCreatedAt() != null ? 
            review.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : 
            null);
        return dto;
    }
}

