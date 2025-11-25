package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.PaymentCallbackRequestDTO;
import com.shopcuathuy.dto.request.PaymentRequestDTO;
import com.shopcuathuy.dto.response.PaymentCallbackResponseDTO;
import com.shopcuathuy.dto.response.PaymentDetailResponseDTO;
import com.shopcuathuy.dto.response.PaymentMethodResponseDTO;
import com.shopcuathuy.dto.response.PaymentResponseDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.PaymentMethod;
import com.shopcuathuy.entity.PaymentTransaction;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.PaymentMethodRepository;
import com.shopcuathuy.repository.PaymentTransactionRepository;
import com.shopcuathuy.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final VNPayService vnPayService;

    @Value("${vnpay.return-url:http://localhost:3000/payment/callback}")
    private String vnpayReturnUrl;

    @Autowired
    public PaymentController(PaymentMethodRepository paymentMethodRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            OrderRepository orderRepository,
                            VNPayService vnPayService) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.vnPayService = vnPayService;
    }

    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponseDTO>>> getPaymentMethods() {
        List<PaymentMethod> methods = paymentMethodRepository.findByIsActiveTrue();
        List<PaymentMethodResponseDTO> methodDTOs = methods.stream()
            .map(this::convertToDTO)
            .sorted((a, b) -> Integer.compare(
                a.displayOrder != null ? a.displayOrder : 0,
                b.displayOrder != null ? b.displayOrder : 0
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(methodDTOs));
    }

    @PostMapping("/process")
    @Transactional
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> processPayment(
            @RequestBody PaymentRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Order order = orderRepository.findById(request.orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        PaymentMethod method = paymentMethodRepository.findById(request.methodId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        // Create payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setOrder(order);
        transaction.setPaymentMethod(method);
        transaction.setAmount(BigDecimal.valueOf(request.amount));
        transaction.setTransactionCode("TXN" + System.currentTimeMillis());
        transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);

        transaction = paymentTransactionRepository.save(transaction);

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.paymentId = transaction.getId();
        response.transactionId = transaction.getTransactionCode();

        // Check if payment method requires redirect (VNPay, MoMo, etc.)
        if (method.getMethodCode() != null && method.getMethodCode().toLowerCase().contains("vnpay")) {
            // Create VNPay payment URL
            String orderInfo = "Thanh toan don hang " + order.getOrderNumber();
            String paymentUrl = vnPayService.createPaymentUrl(
                transaction.getId(),
                request.amount,
                orderInfo,
                vnpayReturnUrl + "?paymentId=" + transaction.getId()
            );
            response.paymentUrl = paymentUrl;
            response.status = "PENDING";
            response.message = "Redirect to VNPay";
        } else {
            // For other payment methods (COD, bank transfer), process immediately
            transaction.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
            transaction = paymentTransactionRepository.save(transaction);
            order.setPaymentStatus(Order.PaymentStatus.PAID);
            orderRepository.save(order);
            response.status = "SUCCESS";
            response.message = "Payment processed successfully";
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDetailResponseDTO>> getPayment(
            @PathVariable String paymentId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        PaymentTransaction transaction = paymentTransactionRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (userId != null && transaction.getOrder() != null && 
            transaction.getOrder().getCustomer() != null &&
            !transaction.getOrder().getCustomer().getId().equals(userId)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertPaymentToDTO(transaction)));
    }

    @PostMapping("/callback")
    @Transactional
    public ResponseEntity<ApiResponse<PaymentCallbackResponseDTO>> paymentCallback(
            @RequestBody PaymentCallbackRequestDTO request) {
        
        PaymentTransaction transaction = paymentTransactionRepository.findById(request.paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // Update payment status based on callback
        if ("SUCCESS".equals(request.status) || "COMPLETED".equals(request.status)) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
            if (transaction.getOrder() != null) {
                transaction.getOrder().setPaymentStatus(Order.PaymentStatus.PAID);
                orderRepository.save(transaction.getOrder());
            }
        } else if ("FAILED".equals(request.status)) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
        }

        if (request.transactionId != null) {
            transaction.setBankTransactionId(request.transactionId);
        }

        transaction = paymentTransactionRepository.save(transaction);

        PaymentCallbackResponseDTO response = new PaymentCallbackResponseDTO();
        response.paymentId = transaction.getId();
        response.status = transaction.getStatus().name();
        response.message = "Payment status updated";

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vnpay/callback")
    @Transactional
    public ResponseEntity<ApiResponse<PaymentCallbackResponseDTO>> vnpayCallback(
            @RequestParam Map<String, String> allParams) {
        
        // Verify VNPay signature
        Map<String, String> verifyResult = vnPayService.verifyPayment(allParams);
        
        if (!Boolean.parseBoolean(verifyResult.get("isValid"))) {
            PaymentCallbackResponseDTO errorResponse = new PaymentCallbackResponseDTO();
            errorResponse.paymentId = verifyResult.get("orderId");
            errorResponse.status = "FAILED";
            errorResponse.message = "Invalid payment signature";
            return ResponseEntity.ok(ApiResponse.success(errorResponse));
        }

        String orderId = verifyResult.get("orderId");
        PaymentTransaction transaction = paymentTransactionRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        String responseCode = verifyResult.get("responseCode");
        if ("00".equals(responseCode)) {
            // Payment successful
            transaction.setStatus(PaymentTransaction.TransactionStatus.COMPLETED);
            transaction.setBankTransactionId(verifyResult.get("transactionId"));
            if (transaction.getOrder() != null) {
                transaction.getOrder().setPaymentStatus(Order.PaymentStatus.PAID);
                orderRepository.save(transaction.getOrder());
            }
        } else {
            // Payment failed
            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
        }

        transaction = paymentTransactionRepository.save(transaction);

        PaymentCallbackResponseDTO response = new PaymentCallbackResponseDTO();
        response.paymentId = transaction.getId();
        response.status = transaction.getStatus().name();
        response.message = verifyResult.get("message");

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private PaymentMethodResponseDTO convertToDTO(PaymentMethod method) {
        PaymentMethodResponseDTO dto = new PaymentMethodResponseDTO();
        dto.id = method.getId();
        dto.name = method.getMethodName();
        dto.description = method.getDescription();
        dto.icon = method.getIcon();
        dto.displayOrder = 0; // Not stored in entity
        dto.isActive = method.getIsActive();
        dto.requiresRedirect = method.getMethodCode() != null && 
            (method.getMethodCode().contains("vnpay") || method.getMethodCode().contains("momo"));
        return dto;
    }

    private PaymentDetailResponseDTO convertPaymentToDTO(PaymentTransaction transaction) {
        PaymentDetailResponseDTO dto = new PaymentDetailResponseDTO();
        dto.id = transaction.getId();
        dto.orderId = transaction.getOrder() != null ? transaction.getOrder().getId() : null;
        dto.userId = transaction.getOrder() != null && transaction.getOrder().getCustomer() != null ?
            transaction.getOrder().getCustomer().getId() : null;
        dto.methodId = transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().getId() : null;
        dto.methodName = transaction.getPaymentMethod() != null ? 
            transaction.getPaymentMethod().getMethodName() : null;
        dto.amount = transaction.getAmount() != null ? transaction.getAmount().doubleValue() : null;
        dto.status = transaction.getStatus() != null ? transaction.getStatus().name() : null;
        dto.createdAt = transaction.getCreatedAt() != null ?
            transaction.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        dto.completedAt = transaction.getUpdatedAt() != null && 
            transaction.getStatus() == PaymentTransaction.TransactionStatus.COMPLETED ?
            transaction.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }
}

