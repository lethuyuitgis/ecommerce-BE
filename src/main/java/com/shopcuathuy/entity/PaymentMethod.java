package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod extends BaseEntity {
    
    @Column(name = "method_code", unique = true, nullable = false, length = 50)
    private String methodCode; // "cod", "transfer", "momo", "zalopay", "paypal"
    
    @Column(name = "method_name", length = 100)
    private String methodName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 255)
    private String icon;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount;
    
    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;
    
    @Column(name = "fee_percentage", precision = 5, scale = 2)
    private BigDecimal feePercentage = BigDecimal.ZERO;
    
    @Column(name = "fee_fixed", precision = 15, scale = 2)
    private BigDecimal feeFixed = BigDecimal.ZERO;
}

