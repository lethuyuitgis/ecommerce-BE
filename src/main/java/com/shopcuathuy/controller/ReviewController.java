package com.shopcuathuy.controller;

import com.shopcuathuy.admin.dto.PageResponse;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.ReviewDTO;
import com.shopcuathuy.dto.request.CreateReviewRequestDTO;
import com.shopcuathuy.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<ReviewDTO>>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<ReviewDTO> reviews = reviewService.getProductReviews(productId, page, size);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @PostMapping(
        value = "/product/{productId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ReviewDTO>> createReview(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String productId,
            @RequestParam("rating") Integer rating,
            @RequestParam("title") String title,
            @RequestParam("comment") String comment,
            @RequestParam(value = "orderItemId", required = false) String orderItemId,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "videos", required = false) MultipartFile[] videos
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        // Validate that user has purchased the product (orderItemId should be provided)
        // In production, you should verify orderItemId belongs to userId and productId
        if (orderItemId == null || orderItemId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Bạn cần mua sản phẩm trước khi đánh giá"));
        }

        List<MultipartFile> imagesList = images != null ? List.of(images) : List.of();
        List<MultipartFile> videosList = videos != null ? List.of(videos) : List.of();
        ReviewDTO review = reviewService.createReview(
                userId,
                productId,
                rating,
                title,
                comment,
                orderItemId,
                imagesList,
                videosList
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review created", review));
    }

    @PostMapping(value = "/product/{productId}/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ReviewDTO>> createReviewJson(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @PathVariable String productId,
            @RequestBody CreateReviewRequestDTO request
    ) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not authenticated"));
        }

        // Validate that user has purchased the product
        if (request.orderItemId == null || request.orderItemId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Bạn cần mua sản phẩm trước khi đánh giá"));
        }

        ReviewDTO review = reviewService.createReview(
                userId,
                productId,
                request.rating,
                request.title,
                request.comment,
                request.orderItemId,
                List.of(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Review created", review));
    }

    @PostMapping("/{reviewId}/helpful")
    public ResponseEntity<ApiResponse<Void>> markHelpful(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String reviewId
    ) {
        reviewService.markHelpful(userId, reviewId);
        return ResponseEntity.ok(ApiResponse.success("Marked review as helpful", null));
    }

}


