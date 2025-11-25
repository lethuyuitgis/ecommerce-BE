package com.shopcuathuy.dto;

import com.shopcuathuy.admin.dto.RevenuePointDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SellerReportDTO {
    private double totalRevenue;
    private long totalOrders;
    private long completedOrders;
    private int totalProducts;
    private int activeProducts;
    private List<OrderSummary> orders;
    private List<ProductSummary> products;
    private List<RevenuePointDTO> revenueSeries;
    private Map<String, Long> statusBreakdown;
    private List<CustomerSummary> topCustomers;
    private LocalDate startDate;
    private LocalDate endDate;

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

    public long getCompletedOrders() {
        return completedOrders;
    }

    public void setCompletedOrders(long completedOrders) {
        this.completedOrders = completedOrders;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getActiveProducts() {
        return activeProducts;
    }

    public void setActiveProducts(int activeProducts) {
        this.activeProducts = activeProducts;
    }

    public List<OrderSummary> getOrders() {
        return orders;
    }

    public void setOrders(List<OrderSummary> orders) {
        this.orders = orders;
    }

    public List<ProductSummary> getProducts() {
        return products;
    }

    public void setProducts(List<ProductSummary> products) {
        this.products = products;
    }

    public List<RevenuePointDTO> getRevenueSeries() {
        return revenueSeries;
    }

    public void setRevenueSeries(List<RevenuePointDTO> revenueSeries) {
        this.revenueSeries = revenueSeries;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }

    public List<CustomerSummary> getTopCustomers() {
        return topCustomers;
    }

    public void setTopCustomers(List<CustomerSummary> topCustomers) {
        this.topCustomers = topCustomers;
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

    public static class OrderSummary {
        private String orderNumber;
        private String customerName;
        private String status;
        private double finalTotal;
        private LocalDateTime createdAt;

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getFinalTotal() {
            return finalTotal;
        }

        public void setFinalTotal(double finalTotal) {
            this.finalTotal = finalTotal;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class ProductSummary {
        private String name;
        private double price;
        private int quantity;
        private String status;
        private String category;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }

    public static class CustomerSummary {
        private String customerId;
        private String customerName;
        private long orderCount;
        private double totalSpent;
        private LocalDateTime lastOrderAt;

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public long getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(long orderCount) {
            this.orderCount = orderCount;
        }

        public double getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(double totalSpent) {
            this.totalSpent = totalSpent;
        }

        public LocalDateTime getLastOrderAt() {
            return lastOrderAt;
        }

        public void setLastOrderAt(LocalDateTime lastOrderAt) {
            this.lastOrderAt = lastOrderAt;
        }
    }
}

