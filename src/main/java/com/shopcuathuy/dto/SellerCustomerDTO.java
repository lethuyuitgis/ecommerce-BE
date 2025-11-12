package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SellerCustomerDTO {
    private String customerId;
    private String fullName;
    private String email;
    private String phone;
    private Long totalOrders;
    private BigDecimal totalSpent;
    private LocalDateTime lastOrderAt;
}
