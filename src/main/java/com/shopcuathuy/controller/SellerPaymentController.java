package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.PaymentMethod;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.PaymentMethodRepository;
import com.shopcuathuy.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/payment")
public class SellerPaymentController {

    private final PaymentMethodRepository paymentMethodRepository;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerPaymentController(PaymentMethodRepository paymentMethodRepository,
                                   SellerRepository sellerRepository) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        List<PaymentMethod> methods = paymentMethodRepository.findAll();
        List<Map<String, Object>> paymentMethods = methods.stream()
            .map(method -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", method.getId());
                dto.put("name", method.getMethodName());
                dto.put("code", method.getMethodCode());
                dto.put("enabled", method.getIsActive());
                dto.put("icon", method.getIcon());
                return dto;
            })
            .collect(Collectors.toList());

        Map<String, Object> settings = new HashMap<>();
        settings.put("paymentMethods", paymentMethods);
        // Bank info would be stored in seller entity or separate table
        // For now, return empty - can be added to Seller entity later
        settings.put("bankInfo", null);

        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/methods/{methodId}")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePaymentMethod(
            @PathVariable String methodId,
            @RequestBody Map<String, Boolean> request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        PaymentMethod method = paymentMethodRepository.findById(methodId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));

        Boolean enabled = request.get("enabled");
        if (enabled != null) {
            method.setIsActive(enabled);
            paymentMethodRepository.save(method);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", method.getId());
        response.put("name", method.getMethodName());
        response.put("code", method.getMethodCode());
        response.put("enabled", method.getIsActive());
        response.put("icon", method.getIcon());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/bank-info")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, String>>> updateBankInfo(
            @RequestBody Map<String, String> bankInfo,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        // Bank info would be stored in seller entity or separate table
        // For now, just return the received data
        return ResponseEntity.ok(ApiResponse.success(bankInfo));
    }
}

