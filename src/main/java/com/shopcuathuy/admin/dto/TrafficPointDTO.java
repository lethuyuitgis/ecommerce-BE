package com.shopcuathuy.admin.dto;

public class TrafficPointDTO {
    private String date;
    private int views;
    private int visitors;
    private double bounceRate;

    public TrafficPointDTO() {
    }

    public TrafficPointDTO(String date, int views, int visitors, double bounceRate) {
        this.date = date;
        this.views = views;
        this.visitors = visitors;
        this.bounceRate = bounceRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getVisitors() {
        return visitors;
    }

    public void setVisitors(int visitors) {
        this.visitors = visitors;
    }

    public double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(double bounceRate) {
        this.bounceRate = bounceRate;
    }
}




