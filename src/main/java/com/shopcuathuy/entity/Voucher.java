package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vouchers", indexes = {
    @Index(name = "idx_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", columnDefinition = "CHAR(36)")
    private Seller seller;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 30)
    private DiscountType discountType;
    
    @Column(name = "discount_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal discountValue;
    
    @Column(name = "min_purchase_amount", precision = 15, scale = 2)
    private BigDecimal minPurchaseAmount;
    
    @Column(name = "max_uses_per_customer")
    private Integer maxUsesPerCustomer = 1;
    
    @Column(name = "total_uses_limit")
    private Integer totalUsesLimit;
    
    @Column(name = "total_uses")
    private Integer totalUses = 0;
    
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;
    
    @Column(name = "applicable_categories", columnDefinition = "JSON")
    private String applicableCategories; // array of category_ids
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherStatus status = VoucherStatus.ACTIVE;
    
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<VoucherUsage> voucherUsages = new ArrayList<>();
    
    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
    }
    
    public enum VoucherStatus {
        ACTIVE, INACTIVE, EXPIRED
    }
}
