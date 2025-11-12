package com.shopcuathuy.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ConversationDTO {
    private String id;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer sellerUnreadCount;
}
