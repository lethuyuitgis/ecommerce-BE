package com.shopcuathuy.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SellerDTO {
    private String id;
    private String shopName;
    private String shopDescription;
    private String shopAvatar;
    private String shopCover;
    private String shopPhone;
    private String shopEmail;
    private String province;
    private String district;
    private String verificationStatus;
    private BigDecimal rating;
    private Integer totalProducts;
    private Integer totalFollowers;
    private Integer totalOrders;
}








