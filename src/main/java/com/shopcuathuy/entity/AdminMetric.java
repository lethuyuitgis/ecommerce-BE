package com.shopcuathuy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminMetric {
    @Id
    @Builder.Default
    private String id = "default";
    
    private long requestCount;
    private long successCount;
    private Long avgResponseMs;
    
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
