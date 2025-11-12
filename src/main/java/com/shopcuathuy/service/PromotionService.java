package com.shopcuathuy.service;

import com.shopcuathuy.dto.PromotionDTO;
import com.shopcuathuy.entity.Promotion;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.PromotionRepository;
import com.shopcuathuy.repository.SellerRepository;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {
    
    private final PromotionRepository promotionRepository;
    private final SellerRepository sellerRepository;
    
    public Page<PromotionDTO> getActivePromotions(Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        return promotionRepository.findActivePromotions(now, pageable)
                .map(this::toDTO);
    }
    
    public Page<PromotionDTO> getSellerPromotions(String userId, Pageable pageable) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        return promotionRepository.findBySellerId(seller.getId(), pageable)
                .map(this::toDTO);
    }
    
    public PromotionDTO createPromotion(String userId, CreatePromotionRequest request) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        
        Promotion promotion = new Promotion();
        promotion.setSeller(seller);
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setPromotionType(Promotion.PromotionType.valueOf(request.getPromotionType()));
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setMaxDiscountAmount(request.getMaxDiscountAmount());
        promotion.setMinPurchaseAmount(request.getMinPurchaseAmount());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setQuantityLimit(request.getQuantityLimit());
        promotion.setStatus(Promotion.PromotionStatus.SCHEDULED);
        
        // Check if should be active
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(request.getStartDate()) && now.isBefore(request.getEndDate())) {
            promotion.setStatus(Promotion.PromotionStatus.ACTIVE);
        }
        
        promotion = promotionRepository.save(promotion);
        return toDTO(promotion);
    }
    
    public PromotionDTO updatePromotion(String userId, String promotionId, UpdatePromotionRequest request) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        
        if (!promotion.getSeller().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Promotion not found");
        }
        
        if (request.getName() != null) {
            promotion.setName(request.getName());
        }
        if (request.getDescription() != null) {
            promotion.setDescription(request.getDescription());
        }
        if (request.getDiscountValue() != null) {
            promotion.setDiscountValue(request.getDiscountValue());
        }
        if (request.getStartDate() != null) {
            promotion.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            promotion.setEndDate(request.getEndDate());
        }
        
        // Update status based on dates
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(promotion.getStartDate()) && now.isBefore(promotion.getEndDate())) {
            promotion.setStatus(Promotion.PromotionStatus.ACTIVE);
        } else if (now.isAfter(promotion.getEndDate())) {
            promotion.setStatus(Promotion.PromotionStatus.ENDED);
        }
        
        promotion = promotionRepository.save(promotion);
        return toDTO(promotion);
    }
    
    public void deletePromotion(String userId, String promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResourceNotFoundException("Promotion not found"));
        
        if (!promotion.getSeller().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Promotion not found");
        }
        
        promotionRepository.delete(promotion);
    }
    
    private PromotionDTO toDTO(Promotion promotion) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(promotion.getId());
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setPromotionType(promotion.getPromotionType().name());
        dto.setDiscountValue(promotion.getDiscountValue());
        dto.setMaxDiscountAmount(promotion.getMaxDiscountAmount());
        dto.setMinPurchaseAmount(promotion.getMinPurchaseAmount());
        dto.setStartDate(promotion.getStartDate());
        dto.setEndDate(promotion.getEndDate());
        dto.setQuantityLimit(promotion.getQuantityLimit());
        dto.setQuantityUsed(promotion.getQuantityUsed());
        dto.setStatus(promotion.getStatus().name());
        return dto;
    }
    
    public static class CreatePromotionRequest {
        private String name;
        private String description;
        private String promotionType;
        private java.math.BigDecimal discountValue;
        private java.math.BigDecimal maxDiscountAmount;
        private java.math.BigDecimal minPurchaseAmount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer quantityLimit;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getPromotionType() { return promotionType; }
        public void setPromotionType(String promotionType) { this.promotionType = promotionType; }
        public java.math.BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(java.math.BigDecimal discountValue) { this.discountValue = discountValue; }
        public java.math.BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
        public void setMaxDiscountAmount(java.math.BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
        public java.math.BigDecimal getMinPurchaseAmount() { return minPurchaseAmount; }
        public void setMinPurchaseAmount(java.math.BigDecimal minPurchaseAmount) { this.minPurchaseAmount = minPurchaseAmount; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public Integer getQuantityLimit() { return quantityLimit; }
        public void setQuantityLimit(Integer quantityLimit) { this.quantityLimit = quantityLimit; }
    }
    
    public static class UpdatePromotionRequest {
        private String name;
        private String description;
        private java.math.BigDecimal discountValue;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public java.math.BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(java.math.BigDecimal discountValue) { this.discountValue = discountValue; }
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    }
}


