package com.shopcuathuy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ShipmentDTO {
    private String id;
    private String orderId;
    private String trackingNumber;
    private String shippingMethodId;
    private String shippingMethodName;
    private String senderName;
    private String senderPhone;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String recipientProvince;
    private String recipientDistrict;
    private String recipientWard;
    private BigDecimal weight;
    private BigDecimal shippingFee;
    private String status;
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;
}








