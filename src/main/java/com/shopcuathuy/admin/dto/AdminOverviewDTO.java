package com.shopcuathuy.admin.dto;

import java.time.LocalDate;
import java.util.List;

public class AdminOverviewDTO {

    private double totalRevenue;
    private long totalOrders;
    private long totalCustomers;
    private long activeSellers;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TopSellerDTO> topSellers;

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
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

    public long getActiveSellers() {
        return activeSellers;
    }

    public void setActiveSellers(long activeSellers) {
        this.activeSellers = activeSellers;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<TopSellerDTO> getTopSellers() {
        return topSellers;
    }

    public void setTopSellers(List<TopSellerDTO> topSellers) {
        this.topSellers = topSellers;
    }

    public static class TopSellerDTO {
        private String sellerId;
        private String shopName;
        private long orders;
        private double revenue;

        public String getSellerId() {
            return sellerId;
        }

        public void setSellerId(String sellerId) {
            this.sellerId = sellerId;
        }

        public String getShopName() {
            return shopName;
        }

        public void setShopName(String shopName) {
            this.shopName = shopName;
        }

        public long getOrders() {
            return orders;
        }

        public void setOrders(long orders) {
            this.orders = orders;
        }

        public double getRevenue() {
            return revenue;
        }

        public void setRevenue(double revenue) {
            this.revenue = revenue;
        }
    }
}



