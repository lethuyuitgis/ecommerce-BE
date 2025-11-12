package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PaymentTransactionDTO {
    private String id;
    private String orderId;
    private String paymentMethodId;
    private String paymentMethodName;
    private BigDecimal amount;
    private String transactionCode;
    private String status;
    private String bankCode;
    private String bankTransactionId;
    private String errorMessage;
    private LocalDateTime createdAt;
}








