package com.shopcuathuy.admin.dto;

public class SellerAnalyticsOverviewDTO {
    private double revenue;
    private double revenueChange;
    private long orders;
    private double ordersChange;
    private double averageOrderValue;
    private double averageOrderValueChange;
    private double conversionRate;
    private double conversionRateChange;

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getRevenueChange() {
        return revenueChange;
    }

    public void setRevenueChange(double revenueChange) {
        this.revenueChange = revenueChange;
    }

    public long getOrders() {
        return orders;
    }

    public void setOrders(long orders) {
        this.orders = orders;
    }

    public double getOrdersChange() {
        return ordersChange;
    }

    public void setOrdersChange(double ordersChange) {
        this.ordersChange = ordersChange;
    }

    public double getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public double getAverageOrderValueChange() {
        return averageOrderValueChange;
    }

    public void setAverageOrderValueChange(double averageOrderValueChange) {
        this.averageOrderValueChange = averageOrderValueChange;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public double getConversionRateChange() {
        return conversionRateChange;
    }

    public void setConversionRateChange(double conversionRateChange) {
        this.conversionRateChange = conversionRateChange;
    }
}




