package com.shopcuathuy.dto.response;

import java.time.Instant;

public class PaymentDetailResponseDTO {
    public String id;
    public String orderId;
    public String userId;
    public String methodId;
    public String methodName;
    public Double amount;
    public String status;
    public Instant createdAt;
    public Instant completedAt;
}


