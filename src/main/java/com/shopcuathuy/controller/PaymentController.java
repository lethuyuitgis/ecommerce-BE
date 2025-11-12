package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.PaymentTransactionDTO;
import com.shopcuathuy.entity.PaymentMethod;
import com.shopcuathuy.repository.PaymentMethodRepository;
import com.shopcuathuy.service.PaymentService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.service.WishlistService;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    private final PaymentMethodRepository paymentMethodRepository;
    
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getPaymentMethods() {
        List<PaymentMethod> methods = paymentMethodRepository.findByIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }
    
    @PostMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<PaymentTransactionDTO>> createPayment(
            @PathVariable String orderId,
            @RequestBody PaymentService.CreatePaymentRequest request) {
        PaymentTransactionDTO transaction = paymentService.createPayment(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment created", transaction));
    }
    
    @GetMapping("/orders/{orderId}/payment")
    public ResponseEntity<ApiResponse<PaymentTransactionDTO>> getPaymentByOrder(@PathVariable String orderId) {
        PaymentTransactionDTO transaction = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }
    
    @PutMapping("/{transactionId}/status")
    public ResponseEntity<ApiResponse<PaymentTransactionDTO>> updatePaymentStatus(
            @PathVariable String transactionId,
            @RequestBody PaymentService.UpdatePaymentStatusRequest request) {
        PaymentTransactionDTO transaction = paymentService.updatePaymentStatus(transactionId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment status updated", transaction));
    }
}

