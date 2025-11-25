package com.shopcuathuy.dto.response;

import java.time.Instant;

public class MessageConversationResponseDTO {
    public String id;
    public String customerId;
    public String customerName;
    public String customerEmail;
    public String sellerId;
    public String sellerName;
    public String lastMessage;
    public Instant lastMessageAt;
    public int sellerUnreadCount;
    public int customerUnreadCount;
}


