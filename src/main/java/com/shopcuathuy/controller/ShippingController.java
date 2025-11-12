package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ShipmentDTO;
import com.shopcuathuy.dto.TrackingUpdateDTO;
import com.shopcuathuy.entity.ShippingMethod;
import com.shopcuathuy.repository.ShippingMethodRepository;
import com.shopcuathuy.service.ShippingService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShippingController {
    
    private final ShippingService shippingService;
    private final ShippingMethodRepository shippingMethodRepository;
    
    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<ShippingMethod>>> getShippingMethods() {
        List<ShippingMethod> methods = shippingMethodRepository.findByIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.success(methods));
    }
    
    @PutMapping("/methods/{id}/active")
    public ResponseEntity<ApiResponse<ShippingMethod>> updateMethodActive(
            @PathVariable String id,
            @RequestBody Map<String, Object> body
    ) {
        boolean isActive = Boolean.TRUE.equals(body.get("isActive")) || "true".equals(String.valueOf(body.get("isActive")));
        ShippingMethod method = shippingService.updateMethodActive(id, isActive);
        return ResponseEntity.ok(ApiResponse.success("Updated", method));
    }
    
    @PutMapping("/methods/default")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setDefaultMethod(
            @RequestBody Map<String, Object> body
    ) {
        String methodId = (String) body.get("shippingMethodId");
        shippingService.setDefaultMethod(methodId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }
    
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveSettings(
            @RequestBody Map<String, Object> body
    ) {
        boolean freeEnabled = Boolean.TRUE.equals(body.get("freeShippingEnabled"))
                || "true".equals(String.valueOf(body.get("freeShippingEnabled")));
        BigDecimal minOrder = null;
        if (body.get("minOrderValue") != null) {
            try {
                minOrder = new BigDecimal(String.valueOf(body.get("minOrderValue")));
            } catch (Exception ignored) {}
        }
        shippingService.saveSettings(freeEnabled, minOrder);
        return ResponseEntity.ok(ApiResponse.success(Map.of("success", true)));
    }
    
    @PostMapping("/calculate-fee")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateShippingFee(@RequestBody Map<String, Object> request) {
        String shippingMethodId = (String) request.get("shippingMethodId");
        String province = (String) request.get("province");
        String district = (String) request.get("district");
        BigDecimal weight = new BigDecimal(request.get("weight").toString());
        
        BigDecimal fee = shippingService.calculateShippingFee(shippingMethodId, province, district, weight);
        return ResponseEntity.ok(ApiResponse.success(fee));
    }
    
    @PostMapping("/shipments")
    public ResponseEntity<ApiResponse<ShipmentDTO>> createShipment(
            @RequestParam String orderId,
            @RequestBody ShippingService.CreateShipmentRequest request) {
        ShipmentDTO shipment = shippingService.createShipment(orderId, request);
        return ResponseEntity.ok(ApiResponse.success("Shipment created", shipment));
    }
    
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ApiResponse<ShipmentDTO>> trackShipment(@PathVariable String trackingNumber) {
        ShipmentDTO shipment = shippingService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(shipment));
    }
    
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<ShipmentDTO>> getShipmentByOrder(@PathVariable String orderId) {
        ShipmentDTO shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(shipment));
    }
    
    @PutMapping("/shipments/{shipmentId}/status")
    public ResponseEntity<ApiResponse<ShipmentDTO>> updateShipmentStatus(
            @PathVariable String shipmentId,
            @RequestBody ShippingService.UpdateShipmentStatusRequest request) {
        ShipmentDTO shipment = shippingService.updateShipmentStatus(shipmentId, request);
        return ResponseEntity.ok(ApiResponse.success("Status updated", shipment));
    }
    
    @GetMapping("/shipments/{shipmentId}/tracking")
    public ResponseEntity<ApiResponse<List<TrackingUpdateDTO>>> getTrackingUpdates(@PathVariable String shipmentId) {
        List<TrackingUpdateDTO> updates = shippingService.getTrackingUpdates(shipmentId);
        return ResponseEntity.ok(ApiResponse.success(updates));
    }
}




