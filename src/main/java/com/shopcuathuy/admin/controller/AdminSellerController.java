package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminSellerDTO;
import com.shopcuathuy.admin.dto.UpdateSellerStatusRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sellers")
public class AdminSellerController {

    private final AdminService adminService;

    public AdminSellerController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminSellerDTO>> listSellers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status
    ) {
        List<AdminSellerDTO> sellers = adminService.listSellers(q, status);
        adminService.recordRequest(true);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminSellerDTO> updateStatus(@PathVariable String id,
                                                       @RequestBody UpdateSellerStatusRequest request) {
        return adminService.updateSellerStatus(id, request.getStatus())
                .map(seller -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(seller);
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.notFound().build();
                });
    }
}




