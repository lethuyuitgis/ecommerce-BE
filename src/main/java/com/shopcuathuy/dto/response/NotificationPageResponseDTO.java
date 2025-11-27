package com.shopcuathuy.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class NotificationPageResponseDTO {
    private List<NotificationResponseDTO> content;
    private long totalElements;
    private int totalPages;
    private int size;
    private int number;
}




