package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.Voucher;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/vouchers")
public class SellerVoucherController {

    private final VoucherRepository voucherRepository;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerVoucherController(VoucherRepository voucherRepository,
                                    SellerRepository sellerRepository) {
        this.voucherRepository = voucherRepository;
        this.sellerRepository = sellerRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSellerVouchers(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Voucher> voucherPage = voucherRepository.findBySellerIdOrderByCreatedAtDesc(
            seller.getId(), pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", voucherPage.getContent().stream()
            .map(this::convertToDTO)
            .toList());
        response.put("totalElements", voucherPage.getTotalElements());
        response.put("totalPages", voucherPage.getTotalPages());
        response.put("size", voucherPage.getSize());
        response.put("number", voucherPage.getNumber());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private Map<String, Object> convertToDTO(Voucher voucher) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", voucher.getId());
        dto.put("code", voucher.getCode());
        dto.put("name", voucher.getCode());
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

