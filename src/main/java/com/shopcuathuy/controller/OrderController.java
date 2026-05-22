package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.CreateOrderRequestDTO;
import com.shopcuathuy.dto.request.UpdateOrderStatusRequestDTO;
import com.shopcuathuy.dto.response.OrderPageResponseDTO;
import com.shopcuathuy.dto.response.OrderResponseDTO;
import com.shopcuathuy.dto.response.PurchaseStatusResponseDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.OrderTimeline;
import com.shopcuathuy.exception.ForbiddenException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.OrderTimelineRepository;
import com.shopcuathuy.service.OrderService;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final OrderRepository orderRepository;
    private final OrderTimelineRepository orderTimelineRepository;

    @Autowired
    public OrderController(OrderService orderService,
                           OrderRepository orderRepository,
                           OrderTimelineRepository orderTimelineRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.orderTimelineRepository = orderTimelineRepository;
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOrderTimeline(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean ownsAsCustomer = order.getCustomer() != null && order.getCustomer().getId().equals(userId);
        boolean ownsAsSeller = order.getSeller() != null && order.getSeller().getUser() != null
            && order.getSeller().getUser().getId().equals(userId);
        if (!isAdmin && !ownsAsCustomer && !ownsAsSeller) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        }

        List<Map<String, Object>> entries = orderTimelineRepository
            .findByOrderIdOrderByCreatedAtDesc(id).stream()
            .map(this::timelineToMap)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(entries));
    }

    private Map<String, Object> timelineToMap(OrderTimeline t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("status", t.getStatus());
        m.put("note", t.getNote());
        m.put("createdBy", t.getCreatedBy());
        m.put("createdAt", t.getCreatedAt() != null
            ? t.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        return m;
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
            @RequestBody UpdateOrderStatusRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        
        try {
            OrderResponseDTO order = orderService.updateOrderStatus(id, request, userId);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (ForbiddenException e) {
            return ResponseEntity.status(403).body(ApiResponse.error("Access denied"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        }
    }
}
