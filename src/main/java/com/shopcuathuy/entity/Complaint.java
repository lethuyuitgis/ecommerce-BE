package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Complaint extends BaseEntity {

    @Column(name = "reporter_id", nullable = false, columnDefinition = "CHAR(36)")
    private String reporterId;

    @Column(name = "target_id", columnDefinition = "CHAR(36)")
    private String targetId;

    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "product_id", length = 50)
    private String productId;

    @Column(length = 50)
    private String category;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "desired_resolution", length = 255)
    private String desiredResolution;

    @Column(length = 30, nullable = false)
    private String status = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "first_response_at")
    private LocalDateTime firstResponseAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}


