package com.shopcuathuy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String userId;
    private String sellerId;
    private String reportType;
    private String exportFormat;
    private String status;
    private Long durationMs;
    private String notes;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
