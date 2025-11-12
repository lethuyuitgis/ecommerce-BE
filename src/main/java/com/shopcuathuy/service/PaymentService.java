package com.shopcuathuy.service;

import com.shopcuathuy.dto.PaymentTransactionDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.PaymentMethod;
import com.shopcuathuy.entity.PaymentTransaction;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.PaymentMethodRepository;
import com.shopcuathuy.repository.PaymentTransactionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    
    public PaymentTransactionDTO createPayment(String orderId, CreatePaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        
        if (order.getPaymentStatus() != Order.PaymentStatus.PENDING) {
            throw new BadRequestException("Order payment status is not pending");
        }
        
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setOrder(order);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setAmount(order.getFinalTotal());
        transaction.setTransactionCode(generateTransactionCode());
        transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);
        
        // Process payment based on method
        if ("cod".equalsIgnoreCase(paymentMethod.getMethodCode())) {
            // COD - mark as completed (payment on delivery)
            transaction.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
        } else {
            // Other payment methods - integrate with payment gateways
            // For now, simulate payment processing
            processPayment(transaction, request);
        }
        
        transaction = paymentTransactionRepository.save(transaction);
        orderRepository.save(order);
        
        return toDTO(transaction);
    }
    
    public PaymentTransactionDTO getPaymentByOrderId(String orderId) {
        PaymentTransaction transaction = paymentTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found"));
        return toDTO(transaction);
    }
    
    public PaymentTransactionDTO updatePaymentStatus(String transactionId, UpdatePaymentStatusRequest request) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment transaction not found"));
        
        transaction.setStatus(PaymentTransaction.TransactionStatus.valueOf(request.getStatus()));
        
        if (request.getBankTransactionId() != null) {
            transaction.setBankTransactionId(request.getBankTransactionId());
        }
        if (request.getBankCode() != null) {
            transaction.setBankCode(request.getBankCode());
        }
        if (request.getErrorMessage() != null) {
            transaction.setErrorMessage(request.getErrorMessage());
        }
        
        // Update order payment status
        Order order = transaction.getOrder();
        if (PaymentTransaction.TransactionStatus.COMPLETED.name().equals(request.getStatus())) {
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            order.setStatus(Order.OrderStatus.CONFIRMED);
        } else if (PaymentTransaction.TransactionStatus.FAILED.name().equals(request.getStatus())) {
            order.setPaymentStatus(Order.PaymentStatus.FAILED);
        }
        
        transaction = paymentTransactionRepository.save(transaction);
        orderRepository.save(order);
        
        return toDTO(transaction);
    }
    
    private void processPayment(PaymentTransaction transaction, CreatePaymentRequest request) {
        // Simulate payment processing
        // In production, integrate with payment gateways (MoMo, ZaloPay, etc.)
        try {
            // Simulate API call to payment gateway
            Thread.sleep(1000); // Simulate network delay
            
            // For demo, mark as completed
            transaction.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
            transaction.setBankTransactionId(request.getBankTransactionId());
            transaction.getOrder().setPaymentStatus(Order.PaymentStatus.PAID);
            transaction.getOrder().setStatus(Order.OrderStatus.CONFIRMED);
        } catch (Exception e) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
            transaction.setErrorMessage(e.getMessage());
        }
    }
    
    private String generateTransactionCode() {
        return "TXN" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    private PaymentTransactionDTO toDTO(PaymentTransaction transaction) {
        PaymentTransactionDTO dto = new PaymentTransactionDTO();
        dto.setId(transaction.getId());
        dto.setOrderId(transaction.getOrder().getId());
        dto.setPaymentMethodId(transaction.getPaymentMethod().getId());
        dto.setPaymentMethodName(transaction.getPaymentMethod().getMethodName());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionCode(transaction.getTransactionCode());
        dto.setStatus(transaction.getStatus().name());
        dto.setBankCode(transaction.getBankCode());
        dto.setBankTransactionId(transaction.getBankTransactionId());
        dto.setErrorMessage(transaction.getErrorMessage());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
    
    public static class CreatePaymentRequest {
        private String paymentMethodId;
        private String bankTransactionId;
        private String bankCode;
        
        // Getters and setters
        public String getPaymentMethodId() { return paymentMethodId; }
        public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
        public String getBankTransactionId() { return bankTransactionId; }
        public void setBankTransactionId(String bankTransactionId) { this.bankTransactionId = bankTransactionId; }
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    }
    
    public static class UpdatePaymentStatusRequest {
        private String status;
        private String bankTransactionId;
        private String bankCode;
        private String errorMessage;
        
        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBankTransactionId() { return bankTransactionId; }
        public void setBankTransactionId(String bankTransactionId) { this.bankTransactionId = bankTransactionId; }
        public String getBankCode() { return bankCode; }
        public void setBankCode(String bankCode) { this.bankCode = bankCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}








