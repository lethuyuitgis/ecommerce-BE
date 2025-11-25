package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.SellerAnalyticsDashboardDTO;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.service.SellerAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/seller/analytics")
public class SellerAnalyticsController {

    private final AdminService adminService;
    private final SellerAnalyticsService sellerAnalyticsService;

    public SellerAnalyticsController(AdminService adminService,
                                     SellerAnalyticsService sellerAnalyticsService) {
        this.adminService = adminService;
        this.sellerAnalyticsService = sellerAnalyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerAnalyticsDashboardDTO>> getDashboard(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false, defaultValue = "30days") String period
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        SellerAnalyticsDashboardDTO dashboard = sellerAnalyticsService.getDashboard(userId, period);
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}


