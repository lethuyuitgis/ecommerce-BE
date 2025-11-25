package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminSystemMetricsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemController {

    private final AdminService adminService;

    public AdminSystemController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/metrics")
    public ResponseEntity<AdminSystemMetricsDTO> getMetrics() {
        AdminSystemMetricsDTO metrics = adminService.getMetrics();
        adminService.recordRequest(true);
        return ResponseEntity.ok(metrics);
    }
}




