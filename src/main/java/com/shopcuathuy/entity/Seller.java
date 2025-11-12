package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sellers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seller extends BaseEntity {
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true, columnDefinition = "CHAR(36)")
    private User user;
    
    @Column(name = "shop_name", length = 200)
    private String shopName;
    
    @Column(name = "shop_description", columnDefinition = "TEXT")
    private String shopDescription;
    
    @Column(name = "shop_avatar")
    private String shopAvatar;
    
    @Column(name = "shop_cover")
    private String shopCover;
    
    @Column(name = "shop_phone", length = 20)
    private String shopPhone;
    
    @Column(name = "shop_email", length = 100)
    private String shopEmail;
    
    @Column(length = 100)
    private String province;
    
    @Column(length = 100)
    private String district;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;
    
    @Column(name = "verification_document")
    private String verificationDocument;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_products")
    private Integer totalProducts = 0;
    
    @Column(name = "total_followers")
    private Integer totalFollowers = 0;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "response_time")
    private Integer responseTime; // in hours
    
    @Column(name = "response_rate", precision = 5, scale = 2)
    private BigDecimal responseRate = BigDecimal.ZERO;
    
    @Column(name = "on_time_delivery_rate", precision = 5, scale = 2)
    private BigDecimal onTimeDeliveryRate = BigDecimal.ZERO;
    
    @Column(name = "return_rate", precision = 5, scale = 2)
    private BigDecimal returnRate = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Promotion> promotions = new ArrayList<>();
    
    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    private List<Voucher> vouchers = new ArrayList<>();
    
    public enum VerificationStatus {
        UNVERIFIED, PENDING, VERIFIED, REJECTED
    }
}

