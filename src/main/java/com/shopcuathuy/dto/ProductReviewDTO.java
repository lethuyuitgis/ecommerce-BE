package com.shopcuathuy.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductReviewDTO {
    private String id;
    private String productId;
    private String userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
}

