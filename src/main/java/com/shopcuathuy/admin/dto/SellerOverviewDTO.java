package com.shopcuathuy.admin.dto;

public class SellerOverviewDTO {
    private long totalProducts;
    private long totalOrders;
    private long totalCustomers;
    private double totalRevenue;

    public SellerOverviewDTO() {
    }

    public SellerOverviewDTO(long totalProducts, long totalOrders, long totalCustomers, double totalRevenue) {
        this.totalProducts = totalProducts;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}




