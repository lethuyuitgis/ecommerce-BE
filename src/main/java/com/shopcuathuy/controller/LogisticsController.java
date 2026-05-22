package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ShippingHub;
import com.shopcuathuy.entity.TrackingUpdate;
import com.shopcuathuy.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final ShippingService shippingService;

    @GetMapping("/hubs")
    public ResponseEntity<ApiResponse<List<ShippingHub>>> getHubs() {
        return ResponseEntity.ok(ApiResponse.success(shippingService.getAllHubs()));
    }

    @PostMapping("/shipments/{id}/tracking")
    public ResponseEntity<ApiResponse<String>> addTracking(
            @PathVariable String id,
            @RequestParam String status,
            @RequestParam String location,
            @RequestParam(required = false) String description) {
        shippingService.addTrackingUpdate(id, status, location, description);
        return ResponseEntity.ok(ApiResponse.success("Tracking added"));
    }

    @PostMapping("/shipments/{id}/assign-hub")
    public ResponseEntity<ApiResponse<String>> assignToHub(
            @PathVariable String id,
            @RequestParam String hubId) {
        shippingService.assignToHub(id, hubId);
        return ResponseEntity.ok(ApiResponse.success("Assigned to hub"));
    }

    @GetMapping("/shipments/{id}/history")
    public ResponseEntity<ApiResponse<List<TrackingUpdate>>> getHistory(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(shippingService.getTrackingHistory(id)));
    }

    @PatchMapping("/shipments/{id}/cod-status")
    public ResponseEntity<ApiResponse<String>> updateCod(
            @PathVariable String id,
            @RequestParam String status) {
        shippingService.updateCodStatus(id, Shipment.CodStatus.valueOf(status.toUpperCase()));
        return ResponseEntity.ok(ApiResponse.success("COD status updated"));
    }
}
