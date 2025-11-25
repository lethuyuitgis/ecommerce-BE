package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.AdminComplaintDTO;
import com.shopcuathuy.admin.dto.CreateComplaintRequest;
import com.shopcuathuy.admin.dto.UpdateComplaintStatusRequest;
import com.shopcuathuy.dto.request.CreateComplaintMessageRequestDTO;
import com.shopcuathuy.dto.response.ComplaintMessageResponseDTO;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/complaints")
public class AdminComplaintController {

    private final AdminService adminService;

    public AdminComplaintController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminComplaintDTO>> listComplaints(
            @RequestParam(required = false) String status
    ) {
        List<AdminComplaintDTO> complaints = adminService.listComplaints(status);
        adminService.recordRequest(true);
        return ResponseEntity.ok(complaints);
    }

    @PostMapping
    public ResponseEntity<AdminComplaintDTO> createComplaint(@RequestBody CreateComplaintRequest request) {
        AdminComplaintDTO created = adminService.createComplaint(request);
        adminService.recordRequest(true);
        return ResponseEntity.status(201).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminComplaintDTO> updateStatus(@PathVariable String id,
                                                          @RequestBody UpdateComplaintStatusRequest request) {
        return adminService.updateComplaintStatus(id, request.getStatus())
                .map(complaint -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(complaint);
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminComplaintDTO> getComplaint(@PathVariable String id) {
        return adminService.getComplaint(id)
            .map(complaint -> {
                adminService.recordRequest(true);
                return ResponseEntity.ok(complaint);
            })
            .orElseGet(() -> {
                adminService.recordRequest(false);
                return ResponseEntity.notFound().build();
            });
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<ComplaintMessageResponseDTO>> getMessages(@PathVariable String id) {
        List<ComplaintMessageResponseDTO> messages = adminService.listComplaintMessages(id);
        adminService.recordRequest(true);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ComplaintMessageResponseDTO> addMessage(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String adminUserId,
            @RequestBody CreateComplaintMessageRequestDTO request) {
        if (adminUserId == null || adminUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        return adminService.addComplaintMessage(id, adminUserId, "ADMIN", request.content, request.attachments)
            .map(message -> {
                adminService.recordRequest(true);
                return ResponseEntity.ok(message);
            })
            .orElseGet(() -> {
                adminService.recordRequest(false);
                return ResponseEntity.notFound().build();
            });
    }
}




