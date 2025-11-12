package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.SellerCustomerDTO;
import com.shopcuathuy.dto.SellerCustomerDetailDTO;
import com.shopcuathuy.service.SellerCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/customers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SellerCustomerController {

    private final SellerCustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SellerCustomerDTO>>> listCustomers(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SellerCustomerDTO> customers = customerService.getCustomers(userId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<SellerCustomerDetailDTO>> getCustomerDetail(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String customerId) {
        SellerCustomerDetailDTO detail = customerService.getCustomerDetail(userId, customerId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
