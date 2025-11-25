package com.shopcuathuy.dto.response;

import java.util.List;

public class ProductPageResponseDTO {
    public List<ProductResponseDTO> content;
    public Integer totalElements;
    public Integer totalPages;
    public Integer size;
    public Integer number;

    public ProductPageResponseDTO() {
    }

    public ProductPageResponseDTO(List<ProductResponseDTO> content, Integer totalElements,
                                  Integer totalPages, Integer size, Integer number) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.size = size;
        this.number = number;
    }
}


