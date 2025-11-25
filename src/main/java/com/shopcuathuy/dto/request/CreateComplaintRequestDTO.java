package com.shopcuathuy.dto.request;

import java.util.List;

public class CreateComplaintRequestDTO {
    public String orderId;
    public String productId;
    public String sellerId;
    public String category;
    public String title;
    public String description;
    public String desiredResolution;
    public List<String> attachments;
}


