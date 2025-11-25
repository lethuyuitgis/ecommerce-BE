package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateOrderRequestDTO;
import com.shopcuathuy.dto.request.UpdateOrderStatusRequestDTO;
import com.shopcuathuy.dto.response.OrderPageResponseDTO;
import com.shopcuathuy.dto.response.OrderResponseDTO;
import com.shopcuathuy.dto.response.PurchaseStatusResponseDTO;
import com.shopcuathuy.exception.ForbiddenException;
import com.shopcuathuy.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<OrderPageResponseDTO>> getOrders(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        OrderPageResponseDTO result = orderService.getOrders(userId, userRole, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        try {
            OrderResponseDTO order = orderService.getOrderById(id, userId);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }
    }

    @GetMapping("/check-purchase/{productId}")
    public ResponseEntity<ApiResponse<PurchaseStatusResponseDTO>> checkPurchase(
            @PathVariable String productId,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        PurchaseStatusResponseDTO status = orderService.checkPurchase(productId, userId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(
            @RequestBody CreateOrderRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        OrderResponseDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> cancelOrder(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        try {
            OrderResponseDTO order = orderService.cancelOrder(id, userId);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable String id,
            @RequestBody UpdateOrderStatusRequestDTO request) {
        
        try {
            OrderResponseDTO order = orderService.updateOrderStatus(id, request);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
