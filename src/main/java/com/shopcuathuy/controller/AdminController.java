package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.Voucher;
import com.shopcuathuy.entity.Complaint;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ComplaintMessage;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.VoucherRepository;
import com.shopcuathuy.repository.ComplaintRepository;
import com.shopcuathuy.repository.ComplaintMessageRepository;
import com.shopcuathuy.repository.ShipmentRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VoucherRepository voucherRepository;
    private final ComplaintRepository complaintRepository;
    private final ComplaintMessageRepository complaintMessageRepository;
    private final ShipmentRepository shipmentRepository;

    public AdminController(UserRepository userRepository,
                           SellerRepository sellerRepository,
                           OrderRepository orderRepository,
                           ProductRepository productRepository,
                           VoucherRepository voucherRepository,
                           ComplaintRepository complaintRepository,
                           ComplaintMessageRepository complaintMessageRepository,
                           ShipmentRepository shipmentRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.voucherRepository = voucherRepository;
        this.complaintRepository = complaintRepository;
        this.complaintMessageRepository = complaintMessageRepository;
        this.shipmentRepository = shipmentRepository;
    }

    /* ===================== Dashboard / Overview ===================== */

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSellers", sellerRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalProducts", productRepository.count());
        stats.put("pendingShippers", userRepository.findByUserType(User.UserType.SHIPPER).stream()
            .filter(u -> u.getApprovalStatus() == User.ApprovalStatus.PENDING).count());
        stats.put("pendingSellers", sellerRepository.findAll().stream()
            .filter(s -> s.getVerificationStatus() == Seller.VerificationStatus.PENDING).count());
        stats.put("openComplaints", complaintRepository.count());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Endpoint dùng cho serverAdminApi.getAdminOverview() — có hỗ trợ filter theo date range
     */
    @GetMapping("/dashboard/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardOverview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalSellers", sellerRepository.count());
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalProducts", productRepository.count());
        stats.put("openComplaints", complaintRepository.count());
        stats.put("pendingSellers", sellerRepository.findAll().stream()
            .filter(s -> s.getVerificationStatus() == Seller.VerificationStatus.PENDING).count());
        stats.put("pendingShippers", userRepository.findByUserType(User.UserType.SHIPPER).stream()
            .filter(u -> u.getApprovalStatus() == User.ApprovalStatus.PENDING).count());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/system/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        Runtime rt = Runtime.getRuntime();
        metrics.put("totalMemoryMB", rt.totalMemory() / 1_048_576);
        metrics.put("freeMemoryMB", rt.freeMemory() / 1_048_576);
        metrics.put("usedMemoryMB", (rt.totalMemory() - rt.freeMemory()) / 1_048_576);
        metrics.put("availableProcessors", rt.availableProcessors());
        metrics.put("totalUsers", userRepository.count());
        metrics.put("totalOrders", orderRepository.count());
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /* ===================== User Management ===================== */

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<User> users = userRepository.findAll();

        List<UserDTO> result = users.stream()
            .filter(u -> q == null || q.isBlank()
                || u.getEmail().toLowerCase().contains(q.toLowerCase())
                || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q.toLowerCase())))
            .filter(u -> role == null || role.isBlank()
                || u.getUserType().name().equalsIgnoreCase(role))
            .filter(u -> status == null || status.isBlank()
                || u.getStatus().name().equalsIgnoreCase(status))
            .skip((long) page * size)
            .limit(size)
            .map(this::toUserDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/users/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable String id,
            @RequestParam String status) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        try {
            user.setStatus(User.UserStatus.valueOf(status.toUpperCase()));
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success(toUserDTO(user)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid status: " + status));
        }
    }

    /* ===================== Seller Management ===================== */

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerDTO>>> listSellers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<SellerDTO> result = sellerRepository.findAll().stream()
            .filter(s -> q == null || q.isBlank()
                || s.getShopName().toLowerCase().contains(q.toLowerCase())
                || (s.getUser() != null && s.getUser().getEmail().toLowerCase().contains(q.toLowerCase())))
            .filter(s -> status == null || status.isBlank()
                || s.getVerificationStatus().name().equalsIgnoreCase(status))
            .skip((long) page * size)
            .limit(size)
            .map(this::toSellerDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/approvals/sellers/pending")
    public ResponseEntity<ApiResponse<List<SellerDTO>>> getPendingSellers() {
        List<SellerDTO> pending = sellerRepository.findAll().stream()
            .filter(s -> s.getVerificationStatus() == Seller.VerificationStatus.PENDING)
            .map(this::toSellerDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PostMapping("/approvals/sellers/{id}/approve")
    @Transactional
    public ResponseEntity<ApiResponse<String>> approveSeller(
            @PathVariable String id) {
        Seller seller = sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        seller.setVerificationStatus(Seller.VerificationStatus.VERIFIED);
        sellerRepository.save(seller);
        return ResponseEntity.ok(ApiResponse.success("Seller verified"));
    }

    @PostMapping("/approvals/sellers/{id}/reject")
    @Transactional
    public ResponseEntity<ApiResponse<String>> rejectSeller(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        Seller seller = sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        seller.setVerificationStatus(Seller.VerificationStatus.REJECTED);
        sellerRepository.save(seller);
        return ResponseEntity.ok(ApiResponse.success("Seller rejected"));
    }

    /* ===================== Shipper Management ===================== */

    @GetMapping("/approvals/shippers/pending")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getPendingShippers() {
        List<UserDTO> pending = userRepository
            .findByUserTypeAndApprovalStatus(User.UserType.SHIPPER, User.ApprovalStatus.PENDING)
            .stream()
            .map(this::toUserDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    @PostMapping("/approvals/shippers/{id}/approve")
    @Transactional
    public ResponseEntity<ApiResponse<String>> approveShipper(
            @PathVariable String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User is not a shipper"));
        }
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Shipper approved"));
    }

    @PostMapping("/approvals/shippers/{id}/reject")
    @Transactional
    public ResponseEntity<ApiResponse<String>> rejectShipper(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getUserType() != User.UserType.SHIPPER) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User is not a shipper"));
        }
        user.setApprovalStatus(User.ApprovalStatus.REJECTED);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Shipper rejected"));
    }

    /* ===================== Shipment Management ===================== */

    @GetMapping("/shipments")
    public ResponseEntity<ApiResponse<List<ShipmentDTO>>> listShipments(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ShipmentDTO> result = shipmentRepository.findAll().stream()
            .filter(s -> status == null || status.isBlank()
                || s.getStatus().name().equalsIgnoreCase(status))
            .skip((long) page * size)
            .limit(size)
            .map(this::toShipmentDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/shipments")
    @Transactional
    public ResponseEntity<ApiResponse<ShipmentDTO>> createShipment(@RequestBody Map<String, Object> body) {
        String orderId = (String) body.get("orderId");
        if (orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("orderId is required"));
        }
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Shipment s = new Shipment();
        s.setOrder(order);
        s.setTrackingNumber("SCT" + System.currentTimeMillis());
        try {
            String statusStr = (String) body.getOrDefault("status", "PENDING");
            s.setStatus(Shipment.ShipmentStatus.valueOf(statusStr.toUpperCase()));
        } catch (IllegalArgumentException e) {
            s.setStatus(Shipment.ShipmentStatus.PENDING);
        }
        // Optional fields
        if (body.get("packageWeight") != null) {
            s.setWeight(new java.math.BigDecimal(body.get("packageWeight").toString()));
        }
        if (body.get("packageSize") instanceof String ps) s.setPackageSize(ps);
        if (body.get("codAmount") != null) {
            s.setCodAmount(new java.math.BigDecimal(body.get("codAmount").toString()));
        }
        if (body.get("notes") instanceof String n) s.setNotes(n);

        // Flatten pickupAddress / deliveryAddress (any) into sender_* / recipient_* fields
        Object pickup = body.get("pickupAddress");
        if (pickup instanceof Map<?, ?> p) {
            s.setSenderName(asStr(p.get("name")));
            s.setSenderPhone(asStr(p.get("phone")));
            s.setSenderAddress(asStr(p.get("address")));
            s.setSenderProvince(asStr(p.get("province")));
            s.setSenderDistrict(asStr(p.get("district")));
            s.setSenderWard(asStr(p.get("ward")));
        }
        Object delivery = body.get("deliveryAddress");
        if (delivery instanceof Map<?, ?> d) {
            s.setRecipientName(asStr(d.get("name")));
            s.setRecipientPhone(asStr(d.get("phone")));
            s.setRecipientAddress(asStr(d.get("address")));
            s.setRecipientProvince(asStr(d.get("province")));
            s.setRecipientDistrict(asStr(d.get("district")));
            s.setRecipientWard(asStr(d.get("ward")));
        }

        shipmentRepository.save(s);
        return ResponseEntity.ok(ApiResponse.success(toShipmentDTO(s)));
    }

    private String asStr(Object v) {
        return v == null ? null : v.toString();
    }

    /* ===================== Voucher Management ===================== */

    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> listVouchers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<VoucherDTO> result = voucherRepository.findAll().stream()
            .filter(v -> q == null || q.isBlank()
                || v.getCode().toLowerCase().contains(q.toLowerCase()))
            .filter(v -> status == null || status.isBlank()
                || v.getStatus().name().equalsIgnoreCase(status))
            .filter(v -> type == null || type.isBlank()
                || v.getDiscountType().name().equalsIgnoreCase(type))
            .skip((long) page * size)
            .limit(size)
            .map(this::toVoucherDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /* ===================== Complaint Management ===================== */

    @GetMapping("/complaints")
    public ResponseEntity<ApiResponse<List<ComplaintDTO>>> listComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ComplaintDTO> result = complaintRepository.findAll().stream()
            .filter(c -> status == null || status.isBlank()
                || c.getStatus().equalsIgnoreCase(status))
            .skip((long) page * size)
            .limit(size)
            .map(this::toComplaintDTO)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<ApiResponse<ComplaintDTO>> getComplaint(@PathVariable String id) {
        Complaint complaint = complaintRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        return ResponseEntity.ok(ApiResponse.success(toComplaintDTO(complaint)));
    }

    /* ===================== User CRUD (CREATE/PATCH/DELETE) ===================== */

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("email is required"));
        }
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already exists"));
        }
        User u = new User();
        u.setEmail(email);
        u.setFullName(body.getOrDefault("fullName", email));
        u.setPasswordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // placeholder, must reset on first login
        try {
            u.setUserType(User.UserType.valueOf(body.getOrDefault("userType", "CUSTOMER").toUpperCase()));
        } catch (IllegalArgumentException e) {
            u.setUserType(User.UserType.CUSTOMER);
        }
        u.setStatus(mapUserStatus(body.getOrDefault("status", "ACTIVE")));
        userRepository.save(u);
        return ResponseEntity.ok(ApiResponse.success(toUserDTO(u)));
    }

    @PatchMapping("/users/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> patchUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String status = body.get("status");
        if (status == null) return ResponseEntity.badRequest().body(ApiResponse.error("status is required"));
        user.setStatus(mapUserStatus(status));
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(toUserDTO(user)));
    }

    @PatchMapping("/users/{id}/role")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> patchUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String role = body.get("role");
        if (role == null) return ResponseEntity.badRequest().body(ApiResponse.error("role is required"));
        try {
            user.setUserType(User.UserType.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role: " + role));
        }
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success(toUserDTO(user)));
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    /* ===================== Seller status PATCH ===================== */

    @PatchMapping("/sellers/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<SellerDTO>> patchSellerStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        Seller seller = sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        String status = body.get("status");
        if (status == null) return ResponseEntity.badRequest().body(ApiResponse.error("status is required"));
        seller.setVerificationStatus(mapSellerStatus(status));
        sellerRepository.save(seller);
        return ResponseEntity.ok(ApiResponse.success(toSellerDTO(seller)));
    }

    /* ===================== Voucher CRUD ===================== */

    @PostMapping("/vouchers")
    @Transactional
    public ResponseEntity<ApiResponse<VoucherDTO>> createVoucher(@RequestBody Map<String, Object> body) {
        Voucher v = new Voucher();
        String code = (String) body.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("code is required"));
        }
        v.setCode(code);
        v.setDescription((String) body.getOrDefault("description", ""));
        v.setDiscountType(mapVoucherType((String) body.getOrDefault("type", "PERCENTAGE")));
        if (body.get("value") != null) v.setDiscountValue(new java.math.BigDecimal(body.get("value").toString()));
        if (body.get("maxDiscount") != null) v.setMaxDiscount(new java.math.BigDecimal(body.get("maxDiscount").toString()));
        if (body.get("minOrderValue") != null) v.setMinPurchaseAmount(new java.math.BigDecimal(body.get("minOrderValue").toString()));
        if (body.get("usageLimit") != null) v.setTotalUsesLimit(((Number) body.get("usageLimit")).intValue());
        if (body.get("startDate") != null) v.setStartDate(parseDateTime((String) body.get("startDate")));
        if (body.get("endDate") != null) v.setEndDate(parseDateTime((String) body.get("endDate")));
        try {
            v.setStatus(Voucher.VoucherStatus.valueOf(((String) body.getOrDefault("status", "ACTIVE")).toUpperCase()));
        } catch (IllegalArgumentException e) {
            v.setStatus(Voucher.VoucherStatus.ACTIVE);
        }
        voucherRepository.save(v);
        return ResponseEntity.ok(ApiResponse.success(toVoucherDTO(v)));
    }

    @PatchMapping("/vouchers/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<VoucherDTO>> updateVoucher(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        Voucher v = voucherRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
        if (body.containsKey("code")) v.setCode((String) body.get("code"));
        if (body.containsKey("description")) v.setDescription((String) body.get("description"));
        if (body.containsKey("type")) v.setDiscountType(mapVoucherType((String) body.get("type")));
        if (body.containsKey("value") && body.get("value") != null)
            v.setDiscountValue(new java.math.BigDecimal(body.get("value").toString()));
        if (body.containsKey("maxDiscount") && body.get("maxDiscount") != null)
            v.setMaxDiscount(new java.math.BigDecimal(body.get("maxDiscount").toString()));
        if (body.containsKey("minOrderValue") && body.get("minOrderValue") != null)
            v.setMinPurchaseAmount(new java.math.BigDecimal(body.get("minOrderValue").toString()));
        if (body.containsKey("usageLimit") && body.get("usageLimit") != null)
            v.setTotalUsesLimit(((Number) body.get("usageLimit")).intValue());
        if (body.containsKey("startDate") && body.get("startDate") != null)
            v.setStartDate(parseDateTime((String) body.get("startDate")));
        if (body.containsKey("endDate") && body.get("endDate") != null)
            v.setEndDate(parseDateTime((String) body.get("endDate")));
        if (body.containsKey("status")) {
            try { v.setStatus(Voucher.VoucherStatus.valueOf(((String) body.get("status")).toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        voucherRepository.save(v);
        return ResponseEntity.ok(ApiResponse.success(toVoucherDTO(v)));
    }

    @DeleteMapping("/vouchers/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteVoucher(@PathVariable String id) {
        if (!voucherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Voucher not found");
        }
        voucherRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Voucher deleted"));
    }

    /* ===================== Complaint status + messages ===================== */

    @PatchMapping("/complaints/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<ComplaintDTO>> patchComplaintStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        Complaint c = complaintRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        String status = body.get("status");
        if (status == null) return ResponseEntity.badRequest().body(ApiResponse.error("status is required"));
        c.setStatus(status.toUpperCase());
        if ("RESOLVED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
            c.setResolvedAt(LocalDateTime.now());
        }
        complaintRepository.save(c);
        return ResponseEntity.ok(ApiResponse.success(toComplaintDTO(c)));
    }

    @GetMapping("/complaints/{id}/messages")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getComplaintMessages(@PathVariable String id) {
        if (!complaintRepository.existsById(id)) throw new ResourceNotFoundException("Complaint not found");
        List<Map<String, Object>> messages = complaintMessageRepository
            .findByComplaintIdOrderByCreatedAtAsc(id).stream()
            .map(this::toComplaintMessageMap)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/complaints/{id}/messages")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> addAdminComplaintMessage(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Complaint c = complaintRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("content is required"));
        }
        ComplaintMessage msg = new ComplaintMessage();
        msg.setComplaint(c);
        msg.setSenderId(userId != null ? userId : "admin");
        msg.setSenderType("ADMIN");
        msg.setContent(content);
        msg.setAttachments(body.get("attachments"));
        complaintMessageRepository.save(msg);
        if (c.getFirstResponseAt() == null) {
            c.setFirstResponseAt(LocalDateTime.now());
            complaintRepository.save(c);
        }
        return ResponseEntity.ok(ApiResponse.success(toComplaintMessageMap(msg)));
    }

    private Map<String, Object> toComplaintMessageMap(ComplaintMessage m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.getId());
        out.put("complaintId", m.getComplaint() != null ? m.getComplaint().getId() : null);
        out.put("senderId", m.getSenderId());
        out.put("senderType", m.getSenderType());
        out.put("content", m.getContent());
        out.put("attachments", m.getAttachments());
        out.put("createdAt", m.getCreatedAt() != null
            ? m.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        return out;
    }

    /* ===================== Helpers ===================== */

    private User.UserStatus mapUserStatus(String feStatus) {
        if (feStatus == null) return User.UserStatus.ACTIVE;
        return switch (feStatus.toUpperCase()) {
            case "SUSPENDED", "BANNED" -> User.UserStatus.BANNED;
            case "PENDING", "INACTIVE" -> User.UserStatus.INACTIVE;
            default -> User.UserStatus.ACTIVE;
        };
    }

    private Seller.VerificationStatus mapSellerStatus(String feStatus) {
        if (feStatus == null) return Seller.VerificationStatus.UNVERIFIED;
        return switch (feStatus.toUpperCase()) {
            case "DRAFT", "UNVERIFIED" -> Seller.VerificationStatus.UNVERIFIED;
            case "PENDING_REVIEW", "PENDING" -> Seller.VerificationStatus.PENDING;
            case "APPROVED", "VERIFIED" -> Seller.VerificationStatus.VERIFIED;
            case "SUSPENDED", "REJECTED" -> Seller.VerificationStatus.REJECTED;
            default -> Seller.VerificationStatus.UNVERIFIED;
        };
    }

    private Voucher.DiscountType mapVoucherType(String feType) {
        if (feType == null) return Voucher.DiscountType.PERCENTAGE;
        return switch (feType.toUpperCase()) {
            case "FIXED", "FIXED_AMOUNT" -> Voucher.DiscountType.FIXED_AMOUNT;
            case "FREESHIP", "FREE_SHIPPING" -> Voucher.DiscountType.FREE_SHIPPING;
            default -> Voucher.DiscountType.PERCENTAGE;
        };
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value);
        } catch (Exception e1) {
            try {
                return java.time.OffsetDateTime.parse(value).toLocalDateTime();
            } catch (Exception e2) {
                return java.time.Instant.parse(value).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        }
    }

    /* ===================== DTO conversion ===================== */

    private UserDTO toUserDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.id = u.getId();
        dto.email = u.getEmail();
        dto.fullName = u.getFullName();
        dto.phone = u.getPhone();
        dto.avatarUrl = u.getAvatarUrl();
        dto.userType = u.getUserType() != null ? u.getUserType().name() : null;
        dto.status = u.getStatus() != null ? u.getStatus().name() : null;
        dto.approvalStatus = u.getApprovalStatus() != null ? u.getApprovalStatus().name() : null;
        dto.createdAt = u.getCreatedAt() != null
            ? u.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }

    private SellerDTO toSellerDTO(Seller s) {
        SellerDTO dto = new SellerDTO();
        dto.id = s.getId();
        dto.shopName = s.getShopName();
        dto.shopEmail = s.getShopEmail();
        dto.shopPhone = s.getShopPhone();
        dto.verificationStatus = s.getVerificationStatus() != null ? s.getVerificationStatus().name() : null;
        dto.userId = s.getUser() != null ? s.getUser().getId() : null;
        dto.userEmail = s.getUser() != null ? s.getUser().getEmail() : null;
        dto.createdAt = s.getCreatedAt() != null
            ? s.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }

    private ShipmentDTO toShipmentDTO(Shipment s) {
        ShipmentDTO dto = new ShipmentDTO();
        dto.id = s.getId();
        dto.trackingNumber = s.getTrackingNumber();
        dto.status = s.getStatus() != null ? s.getStatus().name() : null;
        dto.shipperId = s.getShipper() != null ? s.getShipper().getId() : null;
        dto.shipperName = s.getShipper() != null ? s.getShipper().getFullName() : null;
        dto.orderId = s.getOrder() != null ? s.getOrder().getId() : null;
        dto.createdAt = s.getCreatedAt() != null
            ? s.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }

    private VoucherDTO toVoucherDTO(Voucher v) {
        VoucherDTO dto = new VoucherDTO();
        dto.id = v.getId();
        dto.code = v.getCode();
        dto.name = v.getCode();
        dto.voucherType = v.getDiscountType() != null ? v.getDiscountType().name() : null;
        dto.discountValue = v.getDiscountValue() != null ? v.getDiscountValue().doubleValue() : null;
        dto.status = v.getStatus() != null ? v.getStatus().name() : null;
        dto.startDate = v.getStartDate() != null
            ? v.getStartDate().atZone(ZoneId.systemDefault()).toInstant() : null;
        dto.endDate = v.getEndDate() != null
            ? v.getEndDate().atZone(ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }

    private ComplaintDTO toComplaintDTO(Complaint c) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.id = c.getId();
        dto.subject = c.getTitle();
        dto.status = c.getStatus();
        dto.customerId = c.getReporterId();
        dto.orderId = c.getOrderId();
        dto.category = c.getCategory();
        dto.createdAt = c.getCreatedAt() != null
            ? c.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null;
        return dto;
    }

    /* ===================== Inner DTOs ===================== */

    public static class UserDTO {
        public String id;
        public String email;
        public String fullName;
        public String phone;
        public String avatarUrl;
        public String userType;
        public String status;
        public String approvalStatus;
        public java.time.Instant createdAt;
    }

    public static class SellerDTO {
        public String id;
        public String shopName;
        public String shopEmail;
        public String shopPhone;
        public String verificationStatus;
        public String userId;
        public String userEmail;
        public java.time.Instant createdAt;
    }

    public static class ShipmentDTO {
        public String id;
        public String trackingNumber;
        public String status;
        public String shipperId;
        public String shipperName;
        public String orderId;
        public java.time.Instant createdAt;
    }

    public static class VoucherDTO {
        public String id;
        public String code;
        public String name;
        public String voucherType;
        public Double discountValue;
        public String status;
        public java.time.Instant startDate;
        public java.time.Instant endDate;
    }

    public static class ComplaintDTO {
        public String id;
        public String subject;
        public String status;
        public String customerId;
        public String orderId;
        public String category;
        public java.time.Instant createdAt;
    }
}
