package com.shopcuathuy.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class TrackingUpdateDTO {
    private String id;
    private String status;
    private String location;
    private String description;
    private LocalDateTime timestamp;
}








