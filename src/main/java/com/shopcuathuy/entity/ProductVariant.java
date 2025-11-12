package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private Product product;
    
    @Column(name = "variant_name", length = 255)
    private String variantName; // e.g., "Red - Size M"
    
    @Column(name = "variant_sku", length = 100)
    private String variantSku;
    
    @Column(name = "variant_price", precision = 15, scale = 2)
    private BigDecimal variantPrice;
    
    @Column(name = "variant_quantity")
    private Integer variantQuantity = 0;
    
    @Column(name = "variant_image")
    private String variantImage;
    
    @Column(columnDefinition = "JSON")
    private String attributes; // {color: "red", size: "M"}
}

