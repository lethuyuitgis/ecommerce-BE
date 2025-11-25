package com.shopcuathuy.admin.dto;

import java.time.Instant;
import java.util.List;

public class AdminComplaintDTO {
    private String id;
    private String reporterId;
    private String targetId;
    private String category;
    private String title;
    private String content;
    private String status;
    private String orderId;
    private String productId;
    private String desiredResolution;
    private List<String> attachments;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant dueAt;
    private Instant firstResponseAt;
    private Instant resolvedAt;
    private Boolean overdue;
    private Long firstResponseMinutes;
    private Long resolutionMinutes;

    public AdminComplaintDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDesiredResolution() {
        return desiredResolution;
    }

    public void setDesiredResolution(String desiredResolution) {
        this.desiredResolution = desiredResolution;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDueAt() {
        return dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

    public Instant getFirstResponseAt() {
        return firstResponseAt;
    }

    public void setFirstResponseAt(Instant firstResponseAt) {
        this.firstResponseAt = firstResponseAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public Long getFirstResponseMinutes() {
        return firstResponseMinutes;
    }

    public void setFirstResponseMinutes(Long firstResponseMinutes) {
        this.firstResponseMinutes = firstResponseMinutes;
    }

    public Long getResolutionMinutes() {
        return resolutionMinutes;
    }

    public void setResolutionMinutes(Long resolutionMinutes) {
        this.resolutionMinutes = resolutionMinutes;
    }
}




