package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class OrderDTO {
    private String id;
    private String orderNumber;
    private String customerId;
    private String customerName;
    private String sellerId;
    private String sellerName;
    private String status;
    private String paymentStatus;
    private String shippingStatus;
    private BigDecimal totalPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal tax;
    private BigDecimal finalTotal;
    private String paymentMethod;
    private String notes;
    private String customerNotes;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
}



