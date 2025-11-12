package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SellerCustomerDetailDTO {
    private String customerId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private LocalDateTime firstOrderAt;
    private LocalDateTime lastOrderAt;
    private Long totalOrders;
    private BigDecimal totalSpent;
    private List<OrderSummaryDTO> recentOrders;

    @Data
    public static class OrderSummaryDTO {
        private String orderId;
        private String orderNumber;
        private BigDecimal finalTotal;
        private String status;
        private LocalDateTime createdAt;
    }
}
