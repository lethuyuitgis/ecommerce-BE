package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminPromotionDTO;
import com.shopcuathuy.admin.dto.CreatePromotionRequest;
import com.shopcuathuy.admin.dto.PageResponse;
import com.shopcuathuy.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/promotions")
public class SellerPromotionController {

    private final AdminService adminService;

    public SellerPromotionController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminPromotionDTO>>> listPromotions(
            @RequestParam(required = false) String sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AdminPromotionDTO> data = adminService.listPromotions(sellerId, page, size);
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminPromotionDTO>> createPromotion(
            @RequestParam(required = false) String sellerId,
            @RequestBody CreatePromotionRequest request
    ) {
        String resolvedSellerId = sellerId != null ? sellerId : "default-seller";
        AdminPromotionDTO created = adminService.createPromotion(resolvedSellerId, request);
        adminService.recordRequest(true);
        return ResponseEntity.status(201).body(ApiResponse.success("Promotion created", created));
    }
}


