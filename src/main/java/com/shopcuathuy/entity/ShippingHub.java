package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "shipping_hubs")
@Getter
@Setter
public class ShippingHub extends BaseEntity {
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 20)
    private String code;
    
    @Column(length = 100)
    private String province;
    
    @Column(length = 100)
    private String district;
    
    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "hub_type")
    @Enumerated(EnumType.STRING)
    private HubType hubType = HubType.LOCAL_STATION;
    
    public enum HubType {
        WAREHOUSE, SORTING_CENTER, LOCAL_STATION
    }
}
