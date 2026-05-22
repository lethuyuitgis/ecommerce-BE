package com.shopcuathuy.dto.response;

import java.time.Instant;
import java.util.List;

public class OrderResponseDTO {
    public String id;
    public String orderNumber;
    public String customerId;
    public String customerName;
    public String sellerId;
    public String sellerName;
    public String status;
    public String paymentStatus;
    public String shippingStatus;
    public Double totalPrice;
    public Double subtotal;
    public Double discountAmount;
    public Double shippingFee;
    public Double tax;
    public Double finalTotal;
    public String paymentMethod;
    public String notes;
    public String customerNotes;
    public String recipientName;
    public String recipientPhone;
    public String recipientEmail;
    public String recipientAddress;
    public String recipientProvince;
    public String recipientDistrict;
    public String recipientWard;
    public Instant createdAt;
    public List<OrderItemResponseDTO> items;
}


