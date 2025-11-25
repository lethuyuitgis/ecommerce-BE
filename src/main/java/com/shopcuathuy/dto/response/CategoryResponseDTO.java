package com.shopcuathuy.dto.response;

import java.util.List;

public class CategoryResponseDTO {
    public String id;
    public String name;
    public String slug;
    public String description;
    public String icon;
    public String coverImage;
    public String parentId;
    public Integer displayOrder;
    public Boolean isActive;
    public List<CategoryResponseDTO> children;
    public List<String> subcategories;
}


