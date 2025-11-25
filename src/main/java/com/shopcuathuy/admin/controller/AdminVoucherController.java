package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminVoucherDTO;
import com.shopcuathuy.admin.dto.CreateVoucherRequest;
import com.shopcuathuy.admin.dto.UpdateVoucherRequest;
import com.shopcuathuy.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
public class AdminVoucherController {

    private final AdminService adminService;

    public AdminVoucherController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminVoucherDTO>>> listVouchers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type
    ) {
        List<AdminVoucherDTO> vouchers = adminService.listVouchers(q, status, type);
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(vouchers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminVoucherDTO>> createVoucher(@RequestBody CreateVoucherRequest request) {
        AdminVoucherDTO created = adminService.createVoucher(request);
        adminService.recordRequest(true);
        return ResponseEntity.status(201).body(ApiResponse.success("Voucher created", created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminVoucherDTO>> updateVoucher(@PathVariable String id,
                                                                      @RequestBody UpdateVoucherRequest request) {
        return adminService.updateVoucher(id, request)
                .map(voucher -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(ApiResponse.success("Voucher updated", voucher));
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.status(404).body(ApiResponse.error("Voucher not found"));
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable String id) {
        boolean deleted = adminService.deleteVoucher(id);
        adminService.recordRequest(deleted);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Voucher deleted", null));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Voucher not found"));
    }
}


