package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_recipient_id", columnList = "recipient_id"),
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false, columnDefinition = "CHAR(36)")
    private User recipient;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "related_id", length = 36)
    private String relatedId; // order_id, promotion_id, etc
    
    @Column(name = "link_url")
    private String linkUrl;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    public enum NotificationType {
        ORDER_NEW, ORDER_STATUS, PROMOTION, SYSTEM, COMPLAINT
    }
}
