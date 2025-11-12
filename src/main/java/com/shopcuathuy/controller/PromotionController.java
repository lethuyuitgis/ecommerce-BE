package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.PromotionDTO;
import com.shopcuathuy.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PromotionController {
    
    private final PromotionService promotionService;
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<PromotionDTO>>> getActivePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(promotionService.getActivePromotions(pageable)));
    }
    
    @GetMapping("/seller")
    public ResponseEntity<ApiResponse<Page<PromotionDTO>>> getSellerPromotions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(promotionService.getSellerPromotions(userId, pageable)));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<PromotionDTO>> createPromotion(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody PromotionService.CreatePromotionRequest request) {
        PromotionDTO promotion = promotionService.createPromotion(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Promotion created", promotion));
    }
    
    @PutMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<PromotionDTO>> updatePromotion(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String promotionId,
            @RequestBody PromotionService.UpdatePromotionRequest request) {
        PromotionDTO promotion = promotionService.updatePromotion(userId, promotionId, request);
        return ResponseEntity.ok(ApiResponse.success("Promotion updated", promotion));
    }
    
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String promotionId) {
        promotionService.deletePromotion(userId, promotionId);
        return ResponseEntity.ok(ApiResponse.success("Promotion deleted", null));
    }
}


