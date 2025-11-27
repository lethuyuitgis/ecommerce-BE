package com.shopcuathuy.admin.controller;

import com.shopcuathuy.admin.AdminService;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/approvals")
public class AdminApprovalController {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final AdminService adminService;

    public AdminApprovalController(SellerRepository sellerRepository,
                                   UserRepository userRepository,
                                   AdminService adminService) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    /**
     * Lấy danh sách sellers đang chờ phê duyệt
     */
    @GetMapping("/sellers/pending")
    public ResponseEntity<ApiResponse<List<PendingSellerDTO>>> getPendingSellers() {
        List<Seller> pendingSellers = sellerRepository.findByVerificationStatus(Seller.VerificationStatus.PENDING);
        
        List<PendingSellerDTO> dtos = pendingSellers.stream()
            .map(seller -> {
                PendingSellerDTO dto = new PendingSellerDTO();
                dto.setId(seller.getId());
                dto.setUserId(seller.getUser() != null ? seller.getUser().getId() : null);
                dto.setShopName(seller.getShopName());
                dto.setShopEmail(seller.getShopEmail());
                dto.setShopPhone(seller.getShopPhone());
                dto.setProvince(seller.getProvince());
                dto.setDistrict(seller.getDistrict());
                dto.setShopDescription(seller.getShopDescription());
                dto.setStatus(seller.getVerificationStatus() != null ? seller.getVerificationStatus().name() : "PENDING");
                if (seller.getUser() != null) {
                    dto.setUserName(seller.getUser().getFullName());
                    dto.setUserEmail(seller.getUser().getEmail());
                    dto.setUserPhone(seller.getUser().getPhone());
                }
                dto.setCreatedAt(seller.getCreatedAt() != null 
                    ? seller.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() 
                    : null);
                return dto;
            })
            .collect(Collectors.toList());
        
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Lấy danh sách shippers đang chờ phê duyệt
     */
    @GetMapping("/shippers/pending")
    public ResponseEntity<ApiResponse<List<PendingShipperDTO>>> getPendingShippers() {
        List<User> pendingShippers = userRepository.findByUserTypeAndApprovalStatus(
            User.UserType.SHIPPER, 
            User.ApprovalStatus.PENDING
        );
        
        List<PendingShipperDTO> dtos = pendingShippers.stream()
            .map(user -> {
                PendingShipperDTO dto = new PendingShipperDTO();
                dto.setId(user.getId());
                dto.setEmail(user.getEmail());
                dto.setFullName(user.getFullName());
                dto.setPhone(user.getPhone());
                dto.setStatus(user.getApprovalStatus() != null ? user.getApprovalStatus().name() : "PENDING");
                dto.setCreatedAt(user.getCreatedAt() != null 
                    ? user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() 
                    : null);
                return dto;
            })
            .collect(Collectors.toList());
        
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    /**
     * Phê duyệt seller
     */
    @PostMapping("/sellers/{sellerId}/approve")
    @Transactional
    public ResponseEntity<ApiResponse<String>> approveSeller(@PathVariable String sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElse(null);
        
        if (seller == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Seller not found"));
        }
        
        if (seller.getVerificationStatus() != Seller.VerificationStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Seller is not pending approval"));
        }
        
        seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
        sellerRepository.save(seller);
        
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success("Seller đã được phê duyệt thành công"));
    }

    /**
     * Từ chối seller
     */
    @PostMapping("/sellers/{sellerId}/reject")
    @Transactional
    public ResponseEntity<ApiResponse<String>> rejectSeller(
            @PathVariable String sellerId,
            @RequestBody(required = false) RejectRequest request) {
        
        Seller seller = sellerRepository.findById(sellerId)
            .orElse(null);
        
        if (seller == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Seller not found"));
        }
        
        if (seller.getVerificationStatus() != Seller.VerificationStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Seller is not pending approval"));
        }
        
        seller.setVerificationStatus(Seller.VerificationStatus.REJECTED);
        sellerRepository.save(seller);
        
        // Có thể revert user type về CUSTOMER nếu muốn
        if (seller.getUser() != null && seller.getUser().getUserType() == User.UserType.SELLER) {
            seller.getUser().setUserType(User.UserType.CUSTOMER);
            userRepository.save(seller.getUser());
        }
        
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success("Seller đã bị từ chối"));
    }

    /**
     * Phê duyệt shipper
     */
    @PostMapping("/shippers/{userId}/approve")
    @Transactional
    public ResponseEntity<ApiResponse<String>> approveShipper(@PathVariable String userId) {
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("User not found"));
        }
        
        if (user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User is not a shipper"));
        }
        
        if (user.getApprovalStatus() != User.ApprovalStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Shipper is not pending approval"));
        }
        
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        userRepository.save(user);
        
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success("Shipper đã được phê duyệt thành công"));
    }

    /**
     * Từ chối shipper
     */
    @PostMapping("/shippers/{userId}/reject")
    @Transactional
    public ResponseEntity<ApiResponse<String>> rejectShipper(
            @PathVariable String userId,
            @RequestBody(required = false) RejectRequest request) {
        
        User user = userRepository.findById(userId)
            .orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("User not found"));
        }
        
        if (user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("User is not a shipper"));
        }
        
        if (user.getApprovalStatus() != User.ApprovalStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Shipper is not pending approval"));
        }
        
        user.setApprovalStatus(User.ApprovalStatus.REJECTED);
        // Revert về CUSTOMER
        user.setUserType(User.UserType.CUSTOMER);
        userRepository.save(user);
        
        adminService.recordRequest(true);
        return ResponseEntity.ok(ApiResponse.success("Shipper đã bị từ chối"));
    }

    // DTOs
    public static class PendingSellerDTO {
        private String id;
        private String userId;
        private String shopName;
        private String shopEmail;
        private String shopPhone;
        private String province;
        private String district;
        private String shopDescription;
        private String status;
        private String userName;
        private String userEmail;
        private String userPhone;
        private java.time.Instant createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }
        public String getShopEmail() { return shopEmail; }
        public void setShopEmail(String shopEmail) { this.shopEmail = shopEmail; }
        public String getShopPhone() { return shopPhone; }
        public void setShopPhone(String shopPhone) { this.shopPhone = shopPhone; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getShopDescription() { return shopDescription; }
        public void setShopDescription(String shopDescription) { this.shopDescription = shopDescription; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
        public String getUserPhone() { return userPhone; }
        public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
    }

    public static class PendingShipperDTO {
        private String id;
        private String email;
        private String fullName;
        private String phone;
        private String status;
        private java.time.Instant createdAt;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
    }

    public static class RejectRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}




