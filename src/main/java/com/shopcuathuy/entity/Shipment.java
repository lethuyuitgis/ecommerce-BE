package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_tracking_number", columnList = "tracking_number"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_method_id", nullable = false, columnDefinition = "CHAR(36)")
    private ShippingMethod shippingMethod;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_partner_id", columnDefinition = "CHAR(36)")
    private ShippingPartner shippingPartner;
    
    @Column(name = "tracking_number", unique = true, length = 100)
    private String trackingNumber;
    
    @Column(name = "sender_name", length = 100)
    private String senderName;
    
    @Column(name = "sender_phone", length = 20)
    private String senderPhone;

    @Column(name = "sender_address", columnDefinition = "TEXT")
    private String senderAddress;

    @Column(name = "sender_province", length = 100)
    private String senderProvince;

    @Column(name = "sender_district", length = 100)
    private String senderDistrict;

    @Column(name = "sender_ward", length = 100)
    private String senderWard;
    
    @Column(name = "recipient_name", length = 100)
    private String recipientName;
    
    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;
    
    @Column(name = "recipient_address", columnDefinition = "TEXT")
    private String recipientAddress;
    
    @Column(name = "recipient_province", length = 100)
    private String recipientProvince;
    
    @Column(name = "recipient_district", length = 100)
    private String recipientDistrict;
    
    @Column(name = "recipient_ward", length = 100)
    private String recipientWard;
    
    @Column(precision = 10, scale = 3)
    private BigDecimal weight; // in kg

    @Column(name = "package_size", length = 50)
    private String packageSize;
    
    @Column(name = "shipping_fee", precision = 15, scale = 2)
    private BigDecimal shippingFee;
    
    @Column(name = "insurance_fee", precision = 15, scale = 2)
    private BigDecimal insuranceFee;

    @Column(name = "cod_amount", precision = 15, scale = 2)
    private BigDecimal codAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status = ShipmentStatus.PENDING;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrackingUpdate> trackingUpdates = new ArrayList<>();
    
    public enum ShipmentStatus {
        PENDING, READY_FOR_PICKUP, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED, RETURNED
    }
}

