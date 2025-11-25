package com.shopcuathuy.dto.response;

public class AuthResponseDTO {
    public String token;
    public String refreshToken;
    public String userId;
    public String email;
    public String fullName;
    public String userType;
    public String avatarUrl;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String token, String refreshToken, String userId,
                          String email, String fullName, String userType,
                          String avatarUrl) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.userType = userType;
        this.avatarUrl = avatarUrl;
    }
}


