package com.shopcuathuy.dto;
import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String email;
    private String fullName;
    private String userType;
}








