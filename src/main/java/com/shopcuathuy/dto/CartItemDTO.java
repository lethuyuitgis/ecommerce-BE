package com.shopcuathuy.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class CartItemDTO {
    private String id;
    private String productId;
    private String productName;
    private String productImage;
    private BigDecimal productPrice;
    private String variantId;
    private String variantName;
    private BigDecimal variantPrice;
    private Integer quantity;
    private Integer availableQuantity;
}








