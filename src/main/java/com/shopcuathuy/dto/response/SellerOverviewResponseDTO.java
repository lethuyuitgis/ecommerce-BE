package com.shopcuathuy.dto.response;

import lombok.Data;

@Data
public class SellerOverviewResponseDTO {
    private double totalRevenue;
    private String revenueChange;
    private int newOrders;
    private String newOrdersChange;
    private int productsCount;
    private String productsChange;
    private int views;
    private String viewsChange;
}
