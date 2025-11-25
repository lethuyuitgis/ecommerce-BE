package com.shopcuathuy.model;

import java.time.Instant;

public class UserData {
    public String userId;
    public String email;
    public String password;
    public String fullName;
    public String userType;
    public Instant createdAt;

    public UserData(String userId, String email, String password, 
                   String fullName, String userType, Instant createdAt) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.userType = userType;
        this.createdAt = createdAt;
    }
}

