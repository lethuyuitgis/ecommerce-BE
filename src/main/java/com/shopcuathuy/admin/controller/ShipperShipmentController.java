package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminShipmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipperShipmentController {

    private final AdminService adminService;

    public ShipperShipmentController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<AdminShipmentDTO>> listAvailable() {
        List<AdminShipmentDTO> shipments = adminService.listAvailableShipments();
        adminService.recordRequest(true);
        return ResponseEntity.ok(shipments);
    }
}




