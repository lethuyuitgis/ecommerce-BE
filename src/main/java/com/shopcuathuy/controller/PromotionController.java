package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Promotion;
import com.shopcuathuy.repository.PromotionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionRepository promotionRepository;

    public PromotionController(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

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

        public PromotionPageResponseDTO(List<PromotionResponseDTO> content, int totalElements, int totalPages, int size, int number) {
            this.content = content;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.size = size;
            this.number = number;
        }
    }
}

