package com.shopcuathuy.dto.request;

import java.util.List;
import java.util.Map;

public class SellerUpdateProductRequestDTO {
    public String name;
    public String description;
    public Double price;
    public Double comparePrice;
    public Integer quantity;
    public String status;
    public String categoryId;
    public String categoryName;
    public String sku;
    public List<String> images;
    public List<Map<String, Object>> variants; // Changed from Map to List<Map> to accept array
}

