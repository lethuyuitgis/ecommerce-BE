package com.shopcuathuy.admin.dto;

public class RevenuePointDTO {
    private String date;
    private double revenue;
    private double profit;
    private int orders;

    public RevenuePointDTO() {
    }

    public RevenuePointDTO(String date, double revenue, double profit, int orders) {
        this.date = date;
        this.revenue = revenue;
        this.profit = profit;
        this.orders = orders;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }
}




