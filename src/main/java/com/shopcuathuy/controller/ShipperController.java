package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.response.OrderResponseDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipper")
public class ShipperController {

    private final UserRepository userRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;

    public ShipperController(UserRepository userRepository, 
                             ShipmentRepository shipmentRepository,
                             OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<ApiResponse<String>> registerShipper(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra nếu đã là SHIPPER
        if (user.getUserType() == User.UserType.SHIPPER) {
            return ResponseEntity.ok(ApiResponse.success("Bạn đã đăng ký làm shipper. Đang chờ admin phê duyệt."));
        }

        // Chỉ cho phép CUSTOMER đăng ký
        if (user.getUserType() != User.UserType.CUSTOMER) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Chỉ tài khoản khách hàng mới có thể đăng ký làm shipper"));
        }

        // Cập nhật user type và set status PENDING
        user.setUserType(User.UserType.SHIPPER);
        user.setApprovalStatus(User.ApprovalStatus.PENDING);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Đăng ký shipper thành công. Vui lòng chờ admin phê duyệt."));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<String>> getApprovalStatus(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.ok(ApiResponse.success("NOT_SHIPPER"));
        }

        String status = user.getApprovalStatus() != null 
            ? user.getApprovalStatus().name() 
            : "PENDING";
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/shipments")
    public ResponseEntity<ApiResponse<List<Shipment>>> getAssignedShipments(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        List<Shipment> shipments = shipmentRepository.findByShipperId(userId);

        return ResponseEntity.ok(ApiResponse.success(shipments));
    }

    @PutMapping("/shipments/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updateShipmentStatus(
            @PathVariable String id,
            @RequestParam String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        Shipment shipment = shipmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (shipment.getShipper() == null || !shipment.getShipper().getId().equals(userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not assigned to this shipper"));
        }

        try {
            Shipment.ShipmentStatus newStatus = Shipment.ShipmentStatus.valueOf(status.toUpperCase());
            shipment.setStatus(newStatus);
            shipmentRepository.save(shipment);
            return ResponseEntity.ok(ApiResponse.success("Status updated to " + newStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status"));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getAssignedOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthenticated"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders;

        if (status == null || status.isBlank() || status.equalsIgnoreCase("all")) {
            orders = orderRepository.findByShipperId(userId, pageable);
        } else {
            try {
                Order.ShippingStatus shippingStatus = Order.ShippingStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByShipperIdAndShippingStatus(userId, shippingStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status: " + status));
            }
        }

        return ResponseEntity.ok(ApiResponse.success(orders.map(this::convertToOrderDTO)));
    }

    private OrderResponseDTO convertToOrderDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.id = order.getId();
        dto.orderNumber = order.getOrderNumber();
        dto.customerId = order.getCustomer().getId();
        dto.customerName = order.getCustomer().getFullName();
        dto.sellerId = order.getSeller().getId();
        dto.sellerName = order.getSeller().getShopName();
        dto.status = order.getStatus().name();
        dto.paymentStatus = order.getPaymentStatus().name();
        dto.shippingStatus = order.getShippingStatus().name();
        dto.totalPrice = order.getTotalPrice().doubleValue();
        dto.finalTotal = order.getFinalTotal().doubleValue();
        dto.createdAt = order.getCreatedAt() != null ? 
            order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }
}





