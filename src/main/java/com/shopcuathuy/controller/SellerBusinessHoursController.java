package com.shopcuathuy.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/business-hours")
public class SellerBusinessHoursController {

    private final SellerRepository sellerRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SellerBusinessHoursController(SellerRepository sellerRepository,
                                         ObjectMapper objectMapper) {
        this.sellerRepository = sellerRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Map<String, String>>>> getBusinessHours(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        // Business hours stored in seller entity as JSON string
        // For now, return default if not set
        Map<String, Map<String, String>> businessHours = new HashMap<>();
        
        // Try to parse from seller entity if stored
        // If seller has businessHours field, parse it
        // Otherwise, return default
        
        // Default business hours
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (String day : days) {
            Map<String, String> hours = new HashMap<>();
            hours.put("open", "09:00");
            hours.put("close", "18:00");
            businessHours.put(day, hours);
        }

        return ResponseEntity.ok(ApiResponse.success(businessHours));
    }

    @PutMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Map<String, String>>>> updateBusinessHours(
            @RequestBody Map<String, Map<String, String>> businessHours,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        // Business hours would be stored in seller entity as JSON string
        // For now, just validate and return the received data
        // In production, you would add a businessHours field to Seller entity
        // and store as JSON: seller.setBusinessHours(objectMapper.writeValueAsString(businessHours));
        
        sellerRepository.save(seller);
        
        return ResponseEntity.ok(ApiResponse.success(businessHours));
    }
}

