package com.shopcuathuy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMessageRequest {

    private String conversationId;

    @NotNull
    private String recipientId;

    @NotBlank
    private String content;

    private String attachments;
}
