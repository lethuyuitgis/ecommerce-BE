package com.shopcuathuy.admin.dto;

import java.util.Map;

public class CreateShipmentRequest {
    private String orderId;
    private String sellerId;
    private String shipperId;
    private Map<String, Object> pickupAddress;
    private Map<String, Object> deliveryAddress;
    private Double packageWeight;
    private String packageSize;
    private Double codAmount;
    private String notes;
    private String status;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getShipperId() {
        return shipperId;
    }

    public void setShipperId(String shipperId) {
        this.shipperId = shipperId;
    }

    public Map<String, Object> getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(Map<String, Object> pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public Map<String, Object> getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(Map<String, Object> deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Double getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(Double packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public Double getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(Double codAmount) {
        this.codAmount = codAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}




