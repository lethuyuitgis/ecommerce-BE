package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_timeline", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeline extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private Order order;
    
    @Column(nullable = false, length = 50)
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String note;
    
    @Column(name = "created_by", length = 50)
    private String createdBy; // "system", "customer", "seller", "admin"
}

