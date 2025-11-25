package com.shopcuathuy.admin.dto;

public class TrafficSourceDTO {
    private String source;
    private int visitors;

    public TrafficSourceDTO() {
    }

    public TrafficSourceDTO(String source, int visitors) {
        this.source = source;
        this.visitors = visitors;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getVisitors() {
        return visitors;
    }

    public void setVisitors(int visitors) {
        this.visitors = visitors;
    }
}




