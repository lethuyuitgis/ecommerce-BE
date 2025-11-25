package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CheckoutItemRequestDTO;
import com.shopcuathuy.dto.request.CheckoutRequestDTO;
import com.shopcuathuy.dto.response.CheckoutResponseDTO;
import com.shopcuathuy.dto.response.CheckoutValidationResponseDTO;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.UserAddress;
import com.shopcuathuy.entity.Voucher;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.UserAddressRepository;
import com.shopcuathuy.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final ProductRepository productRepository;
    private final UserAddressRepository userAddressRepository;
    private final VoucherRepository voucherRepository;

    @Autowired
    public CheckoutController(ProductRepository productRepository,
                              UserAddressRepository userAddressRepository,
                              VoucherRepository voucherRepository) {
        this.productRepository = productRepository;
        this.userAddressRepository = userAddressRepository;
        this.voucherRepository = voucherRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponseDTO>> checkout(
            @RequestBody CheckoutRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        // Validate checkout data
        if (request.items == null || request.items.isEmpty()) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Cart is empty"));
        }

        if (request.shippingAddressId == null || request.shippingAddressId.isEmpty()) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Shipping address is required"));
        }

        if (request.paymentMethod == null || request.paymentMethod.isEmpty()) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error("Payment method is required"));
        }

        // Validate address exists
        UserAddress address = userAddressRepository.findById(request.shippingAddressId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));

        // Calculate totals from actual products
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CheckoutItemRequestDTO item : request.items) {
            Product product = productRepository.findById(item.productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.productId));
            
            BigDecimal itemPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
            subtotal = subtotal.add(itemPrice.multiply(BigDecimal.valueOf(item.quantity)));
        }

        // Apply voucher if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.voucherCode != null && !request.voucherCode.isEmpty()) {
            Voucher voucher = voucherRepository.findByCodeAndStatus(
                request.voucherCode, 
                Voucher.VoucherStatus.ACTIVE
            ).orElse(null);
            
            if (voucher != null && isVoucherValid(voucher, subtotal)) {
                if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                    discountAmount = subtotal.multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
                } else {
                    discountAmount = voucher.getDiscountValue();
                }
                
                // Ensure discount doesn't exceed subtotal
                if (discountAmount.compareTo(subtotal) > 0) {
                    discountAmount = subtotal;
                }
            }
        }

        BigDecimal shippingFee = BigDecimal.valueOf(30000); // Default shipping fee
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10% tax
        BigDecimal finalTotal = subtotal.subtract(discountAmount).add(shippingFee).add(tax);

        CheckoutResponseDTO response = new CheckoutResponseDTO();
        response.subtotal = subtotal.doubleValue();
        response.discountAmount = discountAmount.doubleValue();
        response.shippingFee = shippingFee.doubleValue();
        response.tax = tax.doubleValue();
        response.finalTotal = finalTotal.doubleValue();
        response.orderId = UUID.randomUUID().toString();
        response.orderNumber = "ORD" + System.currentTimeMillis();
        response.paymentUrl = "/payment/" + response.orderId;

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<CheckoutValidationResponseDTO>> validateCheckout(
            @RequestBody CheckoutRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        CheckoutValidationResponseDTO validation = new CheckoutValidationResponseDTO();
        validation.valid = true;
        validation.errors = new ArrayList<>();

        if (userId == null || userId.isEmpty()) {
            validation.valid = false;
            validation.errors.add("User not authenticated");
        }

        if (request.items == null || request.items.isEmpty()) {
            validation.valid = false;
            validation.errors.add("Cart is empty");
        }

        if (request.shippingAddressId == null || request.shippingAddressId.isEmpty()) {
            validation.valid = false;
            validation.errors.add("Shipping address is required");
        } else {
            // Validate address exists
            if (!userAddressRepository.existsById(request.shippingAddressId)) {
                validation.valid = false;
                validation.errors.add("Shipping address not found");
            }
        }

        if (request.paymentMethod == null || request.paymentMethod.isEmpty()) {
            validation.valid = false;
            validation.errors.add("Payment method is required");
        }

        // Validate product availability
        if (request.items != null) {
            for (CheckoutItemRequestDTO item : request.items) {
                if (item.quantity <= 0) {
                    validation.valid = false;
                    validation.errors.add("Invalid quantity for product: " + item.productId);
                } else {
                    Product product = productRepository.findById(item.productId).orElse(null);
                    if (product == null) {
                        validation.valid = false;
                        validation.errors.add("Product not found: " + item.productId);
                    } else if (product.getQuantity() < item.quantity) {
                        validation.valid = false;
                        validation.errors.add("Insufficient stock for product: " + product.getName());
                    }
                }
            }
        }

        return ResponseEntity.ok(ApiResponse.success(validation));
    }

    private boolean isVoucherValid(Voucher voucher, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate().isAfter(now) || voucher.getEndDate().isBefore(now)) {
            return false;
        }
        if (voucher.getMinPurchaseAmount() != null && 
            subtotal.compareTo(voucher.getMinPurchaseAmount()) < 0) {
            return false;
        }
        if (voucher.getTotalUsesLimit() != null && 
            voucher.getTotalUses() >= voucher.getTotalUsesLimit()) {
            return false;
        }
        return true;
    }
}

