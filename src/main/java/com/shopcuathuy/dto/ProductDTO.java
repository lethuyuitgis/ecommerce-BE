package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class ProductDTO {
    private String id;
    private String name;
    private String description;
    private String sku;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private Integer quantity;
    private Integer minOrder;
    private String status;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalSold;
    private Integer totalViews;
    private Boolean isFeatured;
    private String categoryId;
    private String categoryName;
    private String sellerId;
    private String sellerName;
    private List<String> images;
    private String primaryImage;
}



