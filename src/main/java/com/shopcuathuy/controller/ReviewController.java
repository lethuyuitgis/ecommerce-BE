package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ProductReviewDTO;
import com.shopcuathuy.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ProductReviewDTO>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(reviewService.getProductReviews(productId, pageable)));
    }
    
    @PostMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ProductReviewDTO>> createReview(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String productId,
            @RequestBody ReviewService.CreateReviewRequest request) {
        ProductReviewDTO review = reviewService.createReview(userId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Review created", review));
    }
}

