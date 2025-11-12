package com.shopcuathuy.entity;

import com.shopcuathuy.common.BaseEntity;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id"),
    @Index(name = "idx_transaction_code", columnList = "transaction_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false, columnDefinition = "CHAR(36)")
    private PaymentMethod paymentMethod;
    
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "transaction_code", unique = true, length = 100)
    private String transactionCode;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "bank_code", length = 50)
    private String bankCode;
    
    @Column(name = "bank_transaction_id", length = 100)
    private String bankTransactionId;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED
    }
}

