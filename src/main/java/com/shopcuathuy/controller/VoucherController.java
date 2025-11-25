package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Voucher;
import com.shopcuathuy.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final VoucherRepository voucherRepository;

    @Autowired
    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableVouchers(
            @RequestParam(required = false) Double subtotal) {
        
        LocalDateTime now = LocalDateTime.now();
        BigDecimal subtotalBD = subtotal != null ? BigDecimal.valueOf(subtotal) : BigDecimal.ZERO;
        
        // Find available vouchers (no seller filter for public vouchers)
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(now, null, subtotalBD);
        
        List<Map<String, Object>> voucherDTOs = vouchers.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(voucherDTOs));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateVoucher(
            @RequestBody Map<String, Object> request) {
        
        String code = (String) request.get("code");
        Double subtotal = request.get("subtotal") != null ? 
            ((Number) request.get("subtotal")).doubleValue() : null;

        Map<String, Object> response = new HashMap<>();
        
        if (code == null || code.isBlank()) {
            response.put("valid", false);
            response.put("discountAmount", 0.0);
            response.put("message", "Voucher code is required");
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        LocalDateTime now = LocalDateTime.now();
        Voucher voucher = voucherRepository.findByCode(code)
            .orElse(null);

        if (voucher == null) {
            response.put("valid", false);
            response.put("discountAmount", 0.0);
            response.put("message", "Voucher not found");
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        // Validate voucher
        if (voucher.getStatus() != Voucher.VoucherStatus.ACTIVE) {
            response.put("valid", false);
            response.put("discountAmount", 0.0);
            response.put("message", "Voucher is not active");
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            response.put("valid", false);
            response.put("discountAmount", 0.0);
            response.put("message", "Voucher is expired or not yet available");
            return ResponseEntity.ok(ApiResponse.success(response));
        }

        if (subtotal != null && voucher.getMinPurchaseAmount() != null) {
            if (BigDecimal.valueOf(subtotal).compareTo(voucher.getMinPurchaseAmount()) < 0) {
                response.put("valid", false);
                response.put("discountAmount", 0.0);
                response.put("message", "Minimum purchase amount not met");
                return ResponseEntity.ok(ApiResponse.success(response));
            }
        }

        // Calculate discount
        double discountAmount = 0.0;
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
            discountAmount = subtotal != null ? 
                subtotal * voucher.getDiscountValue().doubleValue() / 100 : 0.0;
            if (voucher.getMaxDiscount() != null) {
                discountAmount = Math.min(discountAmount, voucher.getMaxDiscount().doubleValue());
            }
        } else if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
            discountAmount = voucher.getDiscountValue().doubleValue();
        }

        response.put("valid", true);
        response.put("voucher", convertToDTO(voucher));
        response.put("discountAmount", discountAmount);
        response.put("message", "Voucher is valid");

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Map<String, Object> convertToDTO(Voucher voucher) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", voucher.getId());
        dto.put("code", voucher.getCode());
        dto.put("name", voucher.getCode()); // Use code as name if no separate name field
        dto.put("description", voucher.getDescription());
        dto.put("discountType", voucher.getDiscountType() != null ? voucher.getDiscountType().name() : null);
        dto.put("discountValue", voucher.getDiscountValue() != null ? voucher.getDiscountValue().doubleValue() : null);
        dto.put("maxDiscountAmount", voucher.getMaxDiscount() != null ? voucher.getMaxDiscount().doubleValue() : null);
        dto.put("minPurchaseAmount", voucher.getMinPurchaseAmount() != null ? voucher.getMinPurchaseAmount().doubleValue() : null);
        dto.put("startDate", voucher.getStartDate() != null ?
            voucher.getStartDate().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        dto.put("endDate", voucher.getEndDate() != null ?
            voucher.getEndDate().atZone(java.time.ZoneId.systemDefault()).toInstant() : null);
        dto.put("usageLimit", voucher.getTotalUsesLimit());
        dto.put("usageCount", voucher.getTotalUses());
        dto.put("status", voucher.getStatus() != null ? voucher.getStatus().name() : null);
        return dto;
    }
}

