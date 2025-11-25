package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.time.LocalDate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report_audits", indexes = {
    @Index(name = "idx_report_user", columnList = "user_id"),
    @Index(name = "idx_report_seller", columnList = "seller_id"),
    @Index(name = "idx_report_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportAudit extends BaseEntity {

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "seller_id", columnDefinition = "CHAR(36)")
    private String sellerId;

    @Column(name = "report_type", length = 50)
    private String reportType;

    @Column(name = "export_format", length = 20)
    private String exportFormat;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "period_start")
    private LocalDate periodStart;

    @Column(name = "period_end")
    private LocalDate periodEnd;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(columnDefinition = "TEXT")
    private String notes;
}



