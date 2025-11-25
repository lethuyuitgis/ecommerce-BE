package com.shopcuathuy.dto.response;

import java.time.Instant;
import java.util.List;

public class ComplaintMessageResponseDTO {
    public String id;
    public String complaintId;
    public String senderId;
    public String senderType;
    public String content;
    public List<String> attachments;
    public Instant createdAt;
}


