package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.CreateOrderRequest;
import com.shopcuathuy.dto.OrderDTO;
import com.shopcuathuy.service.OrderService;
import jakarta.validation.Valid;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {
    
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getUserOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(orderService.getUserOrders(userId, pageable)));
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderById(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId) {
        OrderDTO order = orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(ApiResponse.success(order));
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrderStatus(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String orderId,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status is required"));
        }
        OrderDTO order = orderService.updateOrderStatus(orderId, userId, status);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}
