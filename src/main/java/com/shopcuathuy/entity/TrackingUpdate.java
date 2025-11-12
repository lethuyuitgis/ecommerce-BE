package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tracking_updates", indexes = {
    @Index(name = "idx_shipment_id", columnList = "shipment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackingUpdate extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false, columnDefinition = "CHAR(36)")
    private Shipment shipment;
    
    @Column(length = 50)
    private String status;
    
    @Column(length = 255)
    private String location;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
}

