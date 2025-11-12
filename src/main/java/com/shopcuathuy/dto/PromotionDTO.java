package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PromotionDTO {
    private String id;
    private String name;
    private String description;
    private String promotionType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minPurchaseAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer quantityLimit;
    private Integer quantityUsed;
    private String status;
}








