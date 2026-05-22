package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.TrackingUpdate;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.TrackingUpdateRepository;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentsController {

    private final ShipmentRepository shipmentRepository;
    private final TrackingUpdateRepository trackingUpdateRepository;

    public ShipmentsController(ShipmentRepository shipmentRepository,
                               TrackingUpdateRepository trackingUpdateRepository) {
        this.shipmentRepository = shipmentRepository;
        this.trackingUpdateRepository = trackingUpdateRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAvailableShipments() {
        List<Map<String, Object>> result = shipmentRepository.findAll().stream()
            .filter(s -> s.getShipper() == null)
            .filter(s -> s.getStatus() == Shipment.ShipmentStatus.PENDING
                      || s.getStatus() == Shipment.ShipmentStatus.READY_FOR_PICKUP)
            .map(this::toMap)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTracking(@PathVariable String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Tracking number not found"));
        List<TrackingUpdate> updates = trackingUpdateRepository
            .findByShipmentIdOrderByTimestampDesc(shipment.getId());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("shipment", toMap(shipment));
        result.put("updates", updates.stream().map(this::toUpdateMap).collect(Collectors.toList()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private Map<String, Object> toUpdateMap(TrackingUpdate u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("shipmentId", u.getShipment() != null ? u.getShipment().getId() : null);
        m.put("status", u.getStatus());
        m.put("description", u.getDescription());
        m.put("location", u.getLocation());
        m.put("timestamp", u.getTimestamp() != null
            ? u.getTimestamp().atZone(ZoneId.systemDefault()).toInstant() : null);
        return m;
    }

    private Map<String, Object> toMap(Shipment s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("orderId", s.getOrder() != null ? s.getOrder().getId() : null);
        m.put("trackingNumber", s.getTrackingNumber());
        m.put("status", s.getStatus() != null ? s.getStatus().name() : null);
        m.put("shipperId", s.getShipper() != null ? s.getShipper().getId() : null);
        m.put("recipientName", s.getRecipientName());
        m.put("recipientPhone", s.getRecipientPhone());
        m.put("recipientAddress", s.getRecipientAddress());
        m.put("recipientProvince", s.getRecipientProvince());
        m.put("recipientDistrict", s.getRecipientDistrict());
        m.put("recipientWard", s.getRecipientWard());
        m.put("packageWeight", s.getWeight());
        m.put("packageSize", s.getPackageSize());
        m.put("codAmount", s.getCodAmount());
        m.put("notes", s.getNotes());
        m.put("createdAt", s.getCreatedAt() != null
            ? s.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        m.put("updatedAt", s.getUpdatedAt() != null
            ? s.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        return m;
    }
}
