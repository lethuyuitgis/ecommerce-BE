package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.admin.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        List<AdminUserDTO> users = adminService.listUsers(q, role, status);
        adminService.recordRequest(true);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<AdminUserDTO> createUser(@RequestBody CreateUserRequest request) {
        AdminUserDTO created = adminService.createUser(request);
        adminService.recordRequest(true);
        return ResponseEntity.status(201).body(created);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserDTO> updateStatus(@PathVariable String id,
                                                     @RequestBody UpdateUserStatusRequest request) {
        return adminService.updateUserStatus(id, request.getStatus())
                .map(user -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.notFound().build();
                });
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserDTO> updateRole(@PathVariable String id,
                                                   @RequestBody UpdateUserRoleRequest request) {
        return adminService.updateUserRole(id, request.getRole())
                .map(user -> {
                    adminService.recordRequest(true);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    adminService.recordRequest(false);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        boolean deleted = adminService.deleteUser(id);
        adminService.recordRequest(deleted);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}




