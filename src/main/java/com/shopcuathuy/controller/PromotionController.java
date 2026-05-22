package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Promotion;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.PromotionRepository;
import com.shopcuathuy.repository.SellerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final SellerRepository sellerRepository;

    public PromotionController(PromotionRepository promotionRepository,
                               SellerRepository sellerRepository) {
        this.promotionRepository = promotionRepository;
        this.sellerRepository = sellerRepository;
    }

    /* ===================== Public endpoints ===================== */

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PromotionPageResponseDTO>> getActivePromotions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime now = LocalDateTime.now();
        Page<Promotion> promotionPage = promotionRepository.findActivePromotions(now, pageable);

        List<PromotionResponseDTO> content = promotionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PromotionPageResponseDTO response = new PromotionPageResponseDTO(
                content,
                (int) promotionPage.getTotalElements(),
                promotionPage.getTotalPages(),
                size,
                page
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /* ===================== Seller endpoints ===================== */

    @GetMapping("/seller/promotions")
    public ResponseEntity<ApiResponse<PromotionPageResponseDTO>> getSellerPromotions(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotionPage = promotionRepository.findBySellerId(seller.getId(), pageable);

        List<PromotionResponseDTO> content = promotionPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PromotionPageResponseDTO response = new PromotionPageResponseDTO(
                content,
                (int) promotionPage.getTotalElements(),
                promotionPage.getTotalPages(),
                size,
                page
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/seller/promotions")
    @Transactional
    public ResponseEntity<ApiResponse<PromotionResponseDTO>> createPromotion(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody PromotionRequestDTO request
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Promotion promotion = new Promotion();
        promotion.setId(UUID.randomUUID().toString());
        promotion.setSeller(seller);
        applyRequestToPromotion(promotion, request);
        promotion.setStatus(Promotion.PromotionStatus.SCHEDULED);
        promotion.setQuantityUsed(0);

        promotion = promotionRepository.save(promotion);
        return ResponseEntity.ok(ApiResponse.success(convertToDTO(promotion)));
    }

    @GetMapping("/seller/promotions/{id}")
    public ResponseEntity<ApiResponse<PromotionResponseDTO>> getPromotionById(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }

        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (!promotion.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(promotion)));
    }

    @PutMapping("/seller/promotions/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<PromotionResponseDTO>> updatePromotion(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestBody PromotionRequestDTO request
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }

        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (!promotion.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        applyRequestToPromotion(promotion, request);
        promotion = promotionRepository.save(promotion);
        return ResponseEntity.ok(ApiResponse.success(convertToDTO(promotion)));
    }

    @DeleteMapping("/seller/promotions/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }

        Promotion promotion = promotionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (!promotion.getSeller().getId().equals(seller.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        promotionRepository.delete(promotion);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /* ===================== Helper ===================== */

    private void applyRequestToPromotion(Promotion promotion, PromotionRequestDTO req) {
        if (req.name != null) promotion.setName(req.name);
        if (req.description != null) promotion.setDescription(req.description);
        if (req.promotionType != null) {
            try {
                promotion.setPromotionType(Promotion.PromotionType.valueOf(req.promotionType.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        if (req.discountValue != null) promotion.setDiscountValue(BigDecimal.valueOf(req.discountValue));
        if (req.maxDiscountAmount != null) promotion.setMaxDiscountAmount(BigDecimal.valueOf(req.maxDiscountAmount));
        if (req.minPurchaseAmount != null) promotion.setMinPurchaseAmount(BigDecimal.valueOf(req.minPurchaseAmount));
        if (req.startDate != null) promotion.setStartDate(
            java.time.Instant.parse(req.startDate).atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (req.endDate != null) promotion.setEndDate(
            java.time.Instant.parse(req.endDate).atZone(ZoneId.systemDefault()).toLocalDateTime());
        if (req.quantityLimit != null) promotion.setQuantityLimit(req.quantityLimit);
        if (req.status != null) {
            try {
                promotion.setStatus(Promotion.PromotionStatus.valueOf(req.status.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private PromotionResponseDTO convertToDTO(Promotion promotion) {
        PromotionResponseDTO dto = new PromotionResponseDTO();
        dto.id = promotion.getId();
        dto.name = promotion.getName();
        dto.description = promotion.getDescription();
        dto.promotionType = promotion.getPromotionType() != null ? promotion.getPromotionType().name() : null;
        dto.discountValue = promotion.getDiscountValue() != null ? promotion.getDiscountValue().doubleValue() : null;
        dto.maxDiscountAmount = promotion.getMaxDiscountAmount() != null ? promotion.getMaxDiscountAmount().doubleValue() : null;
        dto.minPurchaseAmount = promotion.getMinPurchaseAmount() != null ? promotion.getMinPurchaseAmount().doubleValue() : null;
        dto.startDate = promotion.getStartDate() != null
                ? promotion.getStartDate().atZone(ZoneId.systemDefault()).toInstant()
                : null;
        dto.endDate = promotion.getEndDate() != null
                ? promotion.getEndDate().atZone(ZoneId.systemDefault()).toInstant()
                : null;
        dto.quantityLimit = promotion.getQuantityLimit();
        dto.quantityUsed = promotion.getQuantityUsed();
        dto.status = promotion.getStatus() != null ? promotion.getStatus().name() : null;
        return dto;
    }

    /* ===================== DTOs ===================== */

    public static class PromotionRequestDTO {
        public String name;
        public String description;
        public String promotionType;
        public Double discountValue;
        public Double maxDiscountAmount;
        public Double minPurchaseAmount;
        public String startDate; // ISO-8601 instant string
        public String endDate;
        public Integer quantityLimit;
        public String status;
    }

    public static class PromotionResponseDTO {
        public String id;
        public String name;
        public String description;
        public String promotionType;
        public Double discountValue;
        public Double maxDiscountAmount;
        public Double minPurchaseAmount;
        public java.time.Instant startDate;
        public java.time.Instant endDate;
        public Integer quantityLimit;
        public Integer quantityUsed;
        public String status;
    }

    public static class PromotionPageResponseDTO {
        public List<PromotionResponseDTO> content;
        public int totalElements;
        public int totalPages;
        public int size;
        public int number;

        public PromotionPageResponseDTO(List<PromotionResponseDTO> content, int totalElements,
                                        int totalPages, int size, int number) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.size = size;
            this.number = number;
        }
    }
}
