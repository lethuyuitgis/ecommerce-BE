package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


@Entity
@Table(name = "shipping_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingPartner extends BaseEntity {
    
    @Column(name = "partner_name", nullable = false, length = 100)
    private String partnerName;
    
    @Column(name = "partner_code", unique = true, nullable = false, length = 50)
    private String partnerCode;
    
    @Column(name = "api_key", length = 255)
    private String apiKey;
    
    @Column(name = "api_secret", length = 255)
    private String apiSecret;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
}

