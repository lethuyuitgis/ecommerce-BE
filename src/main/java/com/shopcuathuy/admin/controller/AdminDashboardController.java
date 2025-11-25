package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminOverviewDTO;
import com.shopcuathuy.api.ApiResponse;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewDTO>> getOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        LocalDate start = parseDate(startDate);
        LocalDate end = parseDate(endDate);
        AdminOverviewDTO overview = adminService.getSystemOverview(start, end);
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }
}



