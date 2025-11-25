package com.shopcuathuy.admin.dto;

public class LowStockProductDTO {
    private String name;
    private int stock;
    private String status;

    public LowStockProductDTO() {
    }

    public LowStockProductDTO(String name, int stock, String status) {
        this.name = name;
        this.stock = stock;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}




