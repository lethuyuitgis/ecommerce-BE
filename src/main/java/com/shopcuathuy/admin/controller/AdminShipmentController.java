package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminShipmentDTO;
import com.shopcuathuy.admin.dto.AssignShipmentRequest;
import com.shopcuathuy.admin.dto.CreateShipmentRequest;
import com.shopcuathuy.admin.dto.UpdateShipmentStatusRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/shipments")
public class AdminShipmentController {

    private final AdminService adminService;

    public AdminShipmentController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminShipmentDTO>> listShipments(
            @RequestParam(required = false) String status
    ) {
        List<AdminShipmentDTO> shipments = adminService.listShipments(status);
        adminService.recordRequest(true);
        return ResponseEntity.ok(shipments);
    }

    @PostMapping
    public ResponseEntity<AdminShipmentDTO> createShipment(@RequestBody CreateShipmentRequest request) {
        AdminShipmentDTO created = adminService.createShipment(request);
        adminService.recordRequest(true);
        return ResponseEntity.status(201).body(created);
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<AdminShipmentDTO> assignShipment(@PathVariable String id,
                                                           @RequestBody AssignShipmentRequest request) {
        return adminService.assignShipment(id, request.getShipperId())
                .map(shipment -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(shipment);
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.notFound().build();
                });
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminShipmentDTO> updateStatus(@PathVariable String id,
                                                         @RequestBody UpdateShipmentStatusRequest request) {
        return adminService.updateShipmentStatus(id, request.getStatus())
                .map(shipment -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(shipment);
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.notFound().build();
                });
    }
}




