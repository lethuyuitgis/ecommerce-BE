package com.shopcuathuy.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderItemDTO {
    private String id;
    private String productId;
    private String productName;
    private String productImage;
    private String variantId;
    private String variantName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}








