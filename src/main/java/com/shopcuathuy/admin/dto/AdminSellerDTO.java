package com.shopcuathuy.admin.dto;

import java.time.Instant;

public class AdminSellerDTO {
    private String id;
    private String userId;
    private String shopName;
    private String slug;
    private String status;
    private Instant createdAt;

    public AdminSellerDTO() {
    }

    public AdminSellerDTO(String id, String userId, String shopName, String slug, String status, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.shopName = shopName;
        this.slug = slug;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}




