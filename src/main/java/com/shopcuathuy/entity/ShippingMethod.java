package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "shipping_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingMethod extends BaseEntity {
    
    @Column(unique = true, nullable = false, length = 50)
    private String code; // "ghn", "shopee_express", "ahamove", "j&t"
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 255)
    private String logo;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "min_weight", precision = 10, scale = 3)
    private BigDecimal minWeight; // in kg
    
    @Column(name = "max_weight", precision = 10, scale = 3)
    private BigDecimal maxWeight;
    
    @Column(name = "min_delivery_days")
    private Integer minDeliveryDays;
    
    @Column(name = "max_delivery_days")
    private Integer maxDeliveryDays;
}

