package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.SellerOverviewDTO;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.repository.SellerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/overview")
public class SellerOverviewController {

    private final AdminService adminService;
    private final SellerRepository sellerRepository;

    public SellerOverviewController(AdminService adminService,
                                    SellerRepository sellerRepository) {
        this.adminService = adminService;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SellerOverviewDTO>> getOverview(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String sellerId) {
        
        // If sellerId is provided in query param, use it
        // Otherwise, try to get sellerId from authenticated user
        String targetSellerId = sellerId;
        if (targetSellerId == null || targetSellerId.isBlank()) {
            if (userId != null && !userId.isBlank()) {
                // Get seller ID from user ID
                Seller seller = sellerRepository.findByUserId(userId).orElse(null);
                if (seller != null) {
                    targetSellerId = seller.getId();
                }
            }
        }
        
        SellerOverviewDTO overview = adminService.getSellerOverview(targetSellerId);
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(overview));
    }
}


