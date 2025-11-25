package com.shopcuathuy.admin.dto;

import java.time.Instant;

public class AdminUserDTO {
    private String id;
    private String email;
    private String fullName;
    private String userType;
    private String status;
    private Instant createdAt;

    public AdminUserDTO() {
    }

    public AdminUserDTO(String id, String email, String fullName, String userType, String status, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
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




