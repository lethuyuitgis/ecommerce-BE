package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.ShippingMethod;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.ShippingMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/shipping")
public class SellerShippingController {

    private final ShippingMethodRepository shippingMethodRepository;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerShippingController(ShippingMethodRepository shippingMethodRepository,
                                    SellerRepository sellerRepository) {
        this.shippingMethodRepository = shippingMethodRepository;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getShippingMethods(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        List<ShippingMethod> methods = shippingMethodRepository.findAll();
        List<Map<String, Object>> methodDTOs = methods.stream()
            .map(method -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", method.getId());
                dto.put("code", method.getCode());
                dto.put("name", method.getName());
                dto.put("description", method.getDescription());
                dto.put("fee", 30000.0); // Default fee - can be calculated based on weight/distance
                dto.put("baseFee", 30000.0);
                dto.put("estimatedDays", method.getMaxDeliveryDays() != null ? 
                    method.getMaxDeliveryDays() : 
                    (method.getMinDeliveryDays() != null ? method.getMinDeliveryDays() : 5));
                dto.put("isActive", method.getIsActive());
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(methodDTOs));
    }

    @PutMapping("/methods/{methodId}/toggle")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateShippingMethodActive(
            @PathVariable String methodId,
            @RequestBody Map<String, Boolean> request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        ShippingMethod method = shippingMethodRepository.findById(methodId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));

        Boolean isActive = request.get("isActive");
        if (isActive != null) {
            method.setIsActive(isActive);
            shippingMethodRepository.save(method);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", method.getId());
        response.put("code", method.getCode());
        response.put("name", method.getName());
        response.put("description", method.getDescription());
        response.put("fee", 30000.0);
        response.put("baseFee", 30000.0);
        response.put("estimatedDays", method.getMaxDeliveryDays() != null ? 
            method.getMaxDeliveryDays() : 5);
        response.put("isActive", method.getIsActive());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getShippingSettings(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Map<String, Object> settings = new HashMap<>();
        settings.put("freeShippingEnabled", false); // Default
        settings.put("minOrderValue", null); // Default

        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    @PutMapping("/settings")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveShippingSettings(
            @RequestBody Map<String, Object> settings,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        // Settings would be stored in seller entity or separate table
        // For now, just return the received data
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
}

