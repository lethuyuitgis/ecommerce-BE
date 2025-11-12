package com.shopcuathuy.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationDTO {
    private String id;
    private String type;
    private String title;
    private String message;
    private String relatedId;
    private String linkUrl;
    private String imageUrl;
    private String data;
    private Boolean isRead;
    private LocalDateTime createdAt;
}

