package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory_history", indexes = {
    @Index(name = "idx_inv_history_product", columnList = "product_id"),
    @Index(name = "idx_inv_history_created", columnList = "created_at")
})
public class InventoryHistory extends BaseEntity {

    @Column(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private String productId;

    @Column(name = "variant_id", columnDefinition = "CHAR(36)")
    private String variantId;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(nullable = false, length = 30)
    private String reason; // purchase | return | adjustment | restock | damage

    @Column(name = "reference_id", length = 64)
    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "user_label", length = 100)
    private String userLabel;

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getVariantId() { return variantId; }
    public void setVariantId(String variantId) { this.variantId = variantId; }

    public Integer getQuantityChange() { return quantityChange; }
    public void setQuantityChange(Integer quantityChange) { this.quantityChange = quantityChange; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getUserLabel() { return userLabel; }
    public void setUserLabel(String userLabel) { this.userLabel = userLabel; }
}
