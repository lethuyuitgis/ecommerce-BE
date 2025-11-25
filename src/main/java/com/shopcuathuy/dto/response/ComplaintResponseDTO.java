package com.shopcuathuy.dto.response;

import java.time.Instant;
import java.util.List;

public class ComplaintResponseDTO {
    public String id;
    public String orderId;
    public String productId;
    public String sellerId;
    public String category;
    public String title;
    public String description;
    public String status;
    public String desiredResolution;
    public List<String> attachments;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant dueAt;
    public Instant firstResponseAt;
    public Instant resolvedAt;
    public Boolean overdue;
    public Long firstResponseMinutes;
    public Long resolutionMinutes;
}


