package com.shopcuathuy.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class ProductResponseDTO {
    public String id;
    public String name;
    public String description;
    public String sku;
    public Double price;
    public Double comparePrice;
    public Integer quantity;
    public String status;
    public Double rating;
    public Integer totalReviews;
    public Integer totalSold;
    public Integer totalViews;
    public Boolean isFeatured;
    public Integer featuredPriority;
    public Boolean flashSaleEnabled;
    public Double flashSalePrice;
    public Instant flashSaleStart;
    public Instant flashSaleEnd;
    public Integer flashSaleStock;
    public Integer flashSaleSold;
    public String categoryId;
    public String categoryName;
    public String sellerId;
    public String sellerName;
    public List<String> images;
    public String primaryImage;
    public Map<String, Object> variants;
    public Instant createdAt;
}


