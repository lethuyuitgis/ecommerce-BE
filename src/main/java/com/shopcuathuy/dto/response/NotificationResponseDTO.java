package com.shopcuathuy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import lombok.Data;

@Data
public class NotificationResponseDTO {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type;
    private String linkUrl;
    private String imageUrl;
    private Instant createdAt;
    private boolean read;

    @JsonProperty("isRead")
    public boolean isRead() {
        return read;
    }
}




