package com.shopcuathuy.admin.dto;

public class CustomerLocationDTO {
    private String city;
    private int customers;

    public CustomerLocationDTO() {
    }

    public CustomerLocationDTO(String city, int customers) {
        this.city = city;
        this.customers = customers;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCustomers() {
        return customers;
    }

    public void setCustomers(int customers) {
        this.customers = customers;
    }
}




