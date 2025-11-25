package com.shopcuathuy.dto.response;

import java.util.List;

public class MessagePageResponseDTO {
    public List<ChatMessageResponseDTO> content;
    public long totalElements;
    public int totalPages;
    public int size;
    public int number;
}


