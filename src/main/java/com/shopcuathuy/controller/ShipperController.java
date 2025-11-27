package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipper")
public class ShipperController {

    private final UserRepository userRepository;

    public ShipperController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}




