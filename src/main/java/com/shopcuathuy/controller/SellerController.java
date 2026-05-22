package com.shopcuathuy.controller;

import com.shopcuathuy.dto.admin.SellerAnalyticsDashboardDTO;
import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.response.SellerOverviewResponseDTO;
import com.shopcuathuy.dto.response.SellerResponseDTO;
import com.shopcuathuy.dto.request.CreateSellerRequestDTO;
import com.shopcuathuy.dto.request.UpdateSellerRequestDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.SellerAnalyticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final SellerAnalyticsService sellerAnalyticsService;

    public SellerController(SellerRepository sellerRepository,
                            UserRepository userRepository,
                            OrderRepository orderRepository,
                            ProductRepository productRepository,
                            SellerAnalyticsService sellerAnalyticsService) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.sellerAnalyticsService = sellerAnalyticsService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<SellerResponseDTO>> getProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Optional<Seller> sellerOpt = sellerRepository.findByUserId(userId);
        if (sellerOpt.isEmpty()) {
            return ResponseEntity.status(404)
                .body(ApiResponse.error("Seller profile not found"));
        }

        Seller seller = sellerOpt.get();
        
        // Kiểm tra trạng thái phê duyệt
        if (seller.getVerificationStatus() == Seller.VerificationStatus.PENDING) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Tài khoản seller của bạn đang chờ admin phê duyệt"));
        }
        
        if (seller.getVerificationStatus() == Seller.VerificationStatus.REJECTED) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Tài khoản seller của bạn đã bị từ chối. Vui lòng liên hệ admin"));
        }

        return ResponseEntity.ok(ApiResponse.success(convertToDTO(seller)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerOverviewResponseDTO>> getOverview(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        String sellerId = seller.getId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthAgo = now.minusDays(30);
        LocalDateTime twoMonthsAgo = now.minusDays(60);

        // Current period
        BigDecimal totalRevenue = orderRepository.sumRevenueBySellerIdAndDateRangeAndStatus(
            sellerId, LocalDateTime.of(2000, 1, 1, 0, 0), now, Order.OrderStatus.DELIVERED);
        long newOrders = orderRepository.countBySellerIdAndDateRange(sellerId, monthAgo, now);
        long totalProducts = productRepository.countBySellerId(sellerId);

        // Previous period (for change calculation)
        BigDecimal prevRevenue = orderRepository.sumRevenueBySellerIdAndDateRangeAndStatus(
            sellerId, twoMonthsAgo, monthAgo, Order.OrderStatus.DELIVERED);
        long prevOrders = orderRepository.countBySellerIdAndDateRange(sellerId, twoMonthsAgo, monthAgo);

        SellerOverviewResponseDTO overview = new SellerOverviewResponseDTO();
        overview.setTotalRevenue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0);
        overview.setRevenueChange(formatChange(totalRevenue, prevRevenue));
        overview.setNewOrders((int) newOrders);
        overview.setNewOrdersChange(formatChangeLong(newOrders, prevOrders));
        overview.setProductsCount((int) totalProducts);
        // Products change is not critical to compute here — set to neutral
        overview.setProductsChange("0");
        // Views tracking not implemented yet — set to 0
        overview.setViews(0);
        overview.setViewsChange("0%");

        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    /** Full analytics dashboard (used by seller analytics page) */
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<ApiResponse<SellerAnalyticsDashboardDTO>> getAnalyticsDashboard(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(required = false, defaultValue = "30days") String period) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        SellerAnalyticsDashboardDTO dashboard = sellerAnalyticsService.getDashboard(userId, period);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    /** Overview alias — same as /dashboard but used by some frontend paths */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<SellerOverviewResponseDTO>> getSellerOverview(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return getOverview(userId);
    }

    // ---- helpers ----
    private String formatChange(BigDecimal current, BigDecimal previous) {
        double cur = current != null ? current.doubleValue() : 0.0;
        double prev = previous != null ? previous.doubleValue() : 0.0;
        if (prev <= 0) return cur > 0 ? "+100%" : "0%";
        double change = ((cur - prev) / prev) * 100;
        return String.format("%+.1f%%", change);
    }

    private String formatChangeLong(long current, long previous) {
        if (previous <= 0) return current > 0 ? "+100%" : "0%";
        double change = ((double)(current - previous) / previous) * 100;
        return String.format("%+.1f%%", change);
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<ApiResponse<SellerResponseDTO>> createSeller(
            @RequestBody(required = false) CreateSellerRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        Seller existing = sellerRepository.findByUserId(userId).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(ApiResponse.success(
                "Seller profile already exists", convertToDTO(existing)));
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CreateSellerRequestDTO payload = request != null
            ? request
            : new CreateSellerRequestDTO();

        Seller seller = new Seller();
        seller.setUser(user);
        seller.setShopName(payload.shopName != null ? payload.shopName : user.getFullName());
        seller.setShopDescription(payload.shopDescription);
        seller.setShopPhone(payload.shopPhone);
        seller.setShopEmail(payload.shopEmail != null ? payload.shopEmail : user.getEmail());
        seller.setProvince(payload.province);
        seller.setDistrict(payload.district);
        // Set status to PENDING - cần admin phê duyệt
        seller.setVerificationStatus(Seller.VerificationStatus.PENDING);

        Seller saved = sellerRepository.save(seller);

        if (user.getUserType() != User.UserType.SELLER) {
            user.setUserType(User.UserType.SELLER);
            userRepository.save(user);
        }

        return ResponseEntity.ok(ApiResponse.success("Seller profile created", convertToDTO(saved)));
    }

    @PutMapping("/profile")
    @Transactional
    public ResponseEntity<ApiResponse<SellerResponseDTO>> updateProfile(
            @RequestBody(required = false) UpdateSellerRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        if (request == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Request body is required"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (request.shopName != null) seller.setShopName(request.shopName);
        if (request.shopDescription != null) seller.setShopDescription(request.shopDescription);
        if (request.shopPhone != null) seller.setShopPhone(request.shopPhone);
        if (request.shopEmail != null) seller.setShopEmail(request.shopEmail);
        if (request.province != null) seller.setProvince(request.province);
        if (request.district != null) seller.setDistrict(request.district);
        if (request.shopAvatar != null) seller.setShopAvatar(request.shopAvatar);
        if (request.shopCover != null) seller.setShopCover(request.shopCover);

        Seller updated = sellerRepository.save(seller);
        return ResponseEntity.ok(ApiResponse.success("Seller profile updated", convertToDTO(updated)));
    }

    private SellerResponseDTO convertToDTO(Seller seller) {
        SellerResponseDTO dto = new SellerResponseDTO();
        dto.id = seller.getId();
        dto.userId = seller.getUser() != null ? seller.getUser().getId() : null;
        dto.shopName = seller.getShopName();
        dto.shopDescription = seller.getShopDescription();
        dto.shopAvatar = seller.getShopAvatar();
        dto.shopCover = seller.getShopCover();
        dto.shopPhone = seller.getShopPhone();
        dto.shopEmail = seller.getShopEmail();
        dto.province = seller.getProvince();
        dto.district = seller.getDistrict();
        dto.verificationStatus = seller.getVerificationStatus() != null
            ? seller.getVerificationStatus().name()
            : null;
        dto.rating = seller.getRating() != null ? seller.getRating().doubleValue() : null;
        dto.totalProducts = seller.getTotalProducts();
        dto.totalFollowers = seller.getTotalFollowers();
        dto.totalOrders = seller.getTotalOrders();
        return dto;
    }
}

