package com.shopcuathuy.dto;


import lombok.Data;

@Data
public class UserDTO {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String userType;
}

