package com.shopcuathuy.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MessageDTO {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderName;
    private String content;
    private String attachments;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
