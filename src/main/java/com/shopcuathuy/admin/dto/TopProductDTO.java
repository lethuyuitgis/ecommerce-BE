package com.shopcuathuy.admin.dto;

public class TopProductDTO {
    private String id;
    private String name;
    private String image;
    private int sold;
    private double revenue;
    private String trend;
    private boolean trendUp;

    public TopProductDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public boolean isTrendUp() {
        return trendUp;
    }

    public void setTrendUp(boolean trendUp) {
        this.trendUp = trendUp;
    }
}




