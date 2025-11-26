package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminShipmentDTO;
import com.shopcuathuy.admin.dto.UpdateShipmentStatusRequest;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ShippingPartner;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.ShippingPartnerRepository;
import com.shopcuathuy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shipments")
public class ShipperShipmentController {

    private final AdminService adminService;
    private final ShipmentRepository shipmentRepository;
    private final ShippingPartnerRepository shippingPartnerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ShipperShipmentController(
            AdminService adminService,
            ShipmentRepository shipmentRepository,
            ShippingPartnerRepository shippingPartnerRepository,
            UserRepository userRepository,
            OrderRepository orderRepository) {
        this.adminService = adminService;
        this.shipmentRepository = shipmentRepository;
        this.shippingPartnerRepository = shippingPartnerRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<List<AdminShipmentDTO>> listAvailable() {
        List<AdminShipmentDTO> shipments = adminService.listAvailableShipments();
        adminService.recordRequest(true);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Lấy danh sách đơn hàng được điều phối cho shipper hiện tại
     */
    @GetMapping("/my-shipments")
    public ResponseEntity<ApiResponse<List<AdminShipmentDTO>>> getMyShipments(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false) String status) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        // Kiểm tra user có phải shipper không
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null || user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Only shippers can view shipments"));
        }
        
        // Kiểm tra trạng thái phê duyệt
        if (user.getApprovalStatus() != User.ApprovalStatus.APPROVED) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Tài khoản shipper của bạn chưa được phê duyệt. Vui lòng chờ admin phê duyệt"));
        }

        // Tìm ShippingPartner của shipper này
        Optional<ShippingPartner> partnerOpt = shippingPartnerRepository.findAll().stream()
            .filter(p -> p.getPartnerCode() != null && p.getPartnerCode().equals("SHIPPER_" + userId))
            .findFirst();

        if (partnerOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        ShippingPartner partner = partnerOpt.get();
        List<Shipment> shipments;

        if (status != null && !status.isBlank()) {
            try {
                Shipment.ShipmentStatus shipmentStatus = Shipment.ShipmentStatus.valueOf(status.toUpperCase());
                shipments = shipmentRepository.findByShippingPartnerIdAndStatus(partner.getId(), shipmentStatus);
            } catch (IllegalArgumentException e) {
                shipments = shipmentRepository.findByShippingPartnerId(partner.getId());
            }
        } else {
            shipments = shipmentRepository.findByShippingPartnerId(partner.getId());
        }

        List<AdminShipmentDTO> result = shipments.stream()
            .map(this::toAdminShipmentDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Cập nhật trạng thái shipment (shipper cập nhật)
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AdminShipmentDTO>> updateMyShipmentStatus(
            @PathVariable String id,
            @RequestBody UpdateShipmentStatusRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        // Kiểm tra user có phải shipper không
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null || user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. Only shippers can update shipments"));
        }
        
        // Kiểm tra trạng thái phê duyệt
        if (user.getApprovalStatus() != User.ApprovalStatus.APPROVED) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Tài khoản shipper của bạn chưa được phê duyệt. Vui lòng chờ admin phê duyệt"));
        }

        // Kiểm tra shipment có thuộc về shipper này không
        Shipment shipment = shipmentRepository.findById(id)
            .orElse(null);

        if (shipment == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Shipment not found"));
        }

        // Kiểm tra ownership
        if (shipment.getShippingPartner() == null || 
            !shipment.getShippingPartner().getPartnerCode().equals("SHIPPER_" + userId)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied. This shipment is not assigned to you"));
        }

        // Cập nhật trạng thái
        Optional<AdminShipmentDTO> updated = adminService.updateShipmentStatus(id, request.getStatus());
        
        if (updated.isPresent()) {
            // Cập nhật trạng thái đơn hàng tương ứng
            if (shipment.getOrder() != null) {
                Order order = shipment.getOrder();
                Shipment.ShipmentStatus newStatus = parseShipmentStatus(request.getStatus());
                
                switch (newStatus) {
                    case PENDING:
                        // Không thay đổi trạng thái đơn hàng
                        break;
                    case READY_FOR_PICKUP:
                        order.setShippingStatus(Order.ShippingStatus.PENDING);
                        break;
                    case PICKED_UP:
                        order.setShippingStatus(Order.ShippingStatus.PICKED_UP);
                        break;
                    case IN_TRANSIT:
                        order.setShippingStatus(Order.ShippingStatus.IN_TRANSIT);
                        order.setStatus(Order.OrderStatus.SHIPPED);
                        break;
                    case OUT_FOR_DELIVERY:
                        order.setShippingStatus(Order.ShippingStatus.IN_TRANSIT);
                        break;
                    case DELIVERED:
                        order.setShippingStatus(Order.ShippingStatus.DELIVERED);
                        order.setStatus(Order.OrderStatus.DELIVERED);
                        break;
                    case FAILED:
                        order.setShippingStatus(Order.ShippingStatus.FAILED);
                        break;
                    case RETURNED:
                        // Xử lý trả hàng nếu cần
                        break;
                }
                orderRepository.save(order);
            }
            
            return ResponseEntity.ok(ApiResponse.success(updated.get()));
        } else {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Failed to update shipment status"));
        }
    }

    private Shipment.ShipmentStatus parseShipmentStatus(String status) {
        try {
            return Shipment.ShipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Shipment.ShipmentStatus.PENDING;
        }
    }

    /**
     * Convert Shipment to AdminShipmentDTO
     */
    private AdminShipmentDTO toAdminShipmentDTO(Shipment shipment) {
        AdminShipmentDTO dto = new AdminShipmentDTO();
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrder() != null ? shipment.getOrder().getId() : null);
        dto.setSellerId(
            shipment.getOrder() != null && shipment.getOrder().getSeller() != null
                ? shipment.getOrder().getSeller().getId()
                : null
        );
        dto.setShipperId(shipment.getShippingPartner() != null ? shipment.getShippingPartner().getId() : null);
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setStatus(shipment.getStatus() != null ? shipment.getStatus().name() : null);
        dto.setPickupAddress(buildAddressMap(
            shipment.getSenderName(),
            shipment.getSenderPhone(),
            shipment.getSenderAddress(),
            shipment.getSenderProvince(),
            shipment.getSenderDistrict(),
            shipment.getSenderWard()
        ));
        dto.setDeliveryAddress(buildAddressMap(
            shipment.getRecipientName(),
            shipment.getRecipientPhone(),
            shipment.getRecipientAddress(),
            shipment.getRecipientProvince(),
            shipment.getRecipientDistrict(),
            shipment.getRecipientWard()
        ));
        dto.setPackageWeight(shipment.getWeight() != null ? shipment.getWeight().doubleValue() : null);
        dto.setPackageSize(shipment.getPackageSize());
        dto.setCodAmount(shipment.getCodAmount() != null ? shipment.getCodAmount().doubleValue() : null);
        dto.setNotes(shipment.getNotes());
        dto.setCreatedAt(shipment.getCreatedAt() != null
            ? shipment.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : null);
        dto.setUpdatedAt(shipment.getUpdatedAt() != null
            ? shipment.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : null);
        return dto;
    }

    private Map<String, Object> buildAddressMap(String name, String phone, String address,
                                                String province, String district, String ward) {
        Map<String, Object> map = new HashMap<>();
        if (name != null) map.put("name", name);
        if (phone != null) map.put("phone", phone);
        if (address != null) map.put("address", address);
        if (province != null) map.put("province", province);
        if (district != null) map.put("district", district);
        if (ward != null) map.put("ward", ward);
        return map;
    }
}




