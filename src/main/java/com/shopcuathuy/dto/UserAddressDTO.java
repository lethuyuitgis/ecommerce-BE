package com.shopcuathuy.dto;
import lombok.Data;

@Data
public class UserAddressDTO {
    private String id;
    private String addressType;
    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String street;
    private Boolean isDefault;
}

