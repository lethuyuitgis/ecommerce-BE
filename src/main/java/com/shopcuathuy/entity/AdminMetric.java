package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admin_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminMetric extends BaseEntity {

    @Column(name = "total_products")
    private Long totalProducts = 0L;

    @Column(name = "total_orders")
    private Long totalOrders = 0L;

    @Column(name = "total_customers")
    private Long totalCustomers = 0L;

    @Column(name = "total_revenue", precision = 18, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "request_count")
    private Long requestCount = 0L;

    @Column(name = "success_count")
    private Long successCount = 0L;

    @Column(name = "active_sellers")
    private Long activeSellers = 0L;

    @Column(name = "avg_response_ms")
    private Long avgResponseMs = 0L;
}

