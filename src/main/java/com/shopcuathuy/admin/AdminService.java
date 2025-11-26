package com.shopcuathuy.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopcuathuy.admin.dto.*;
import com.shopcuathuy.dto.response.ComplaintMessageResponseDTO;
import com.shopcuathuy.entity.AdminMetric;
import com.shopcuathuy.entity.Complaint;
import com.shopcuathuy.entity.ComplaintMessage;
import com.shopcuathuy.entity.Notification;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.Promotion;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ShippingMethod;
import com.shopcuathuy.entity.ShippingPartner;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.Voucher;
import com.shopcuathuy.repository.AdminMetricRepository;
import com.shopcuathuy.repository.ComplaintMessageRepository;
import com.shopcuathuy.repository.ComplaintRepository;
import com.shopcuathuy.repository.NotificationRepository;
import com.shopcuathuy.repository.OrderItemRepository;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.PromotionRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.ShippingMethodRepository;
import com.shopcuathuy.repository.ShippingPartnerRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.repository.VoucherRepository;
import com.shopcuathuy.service.NotificationService;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintMessageRepository complaintMessageRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminMetricRepository adminMetricRepository;
    private final VoucherRepository voucherRepository;
    private final ProductRepository productRepository;
    private final PromotionRepository promotionRepository;
    private final SellerRepository sellerRepository;
    private final ShipmentRepository shipmentRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final ShippingPartnerRepository shippingPartnerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public AdminService(ComplaintRepository complaintRepository,
                        ComplaintMessageRepository complaintMessageRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        AdminMetricRepository adminMetricRepository,
                        VoucherRepository voucherRepository,
                        ProductRepository productRepository,
                        PromotionRepository promotionRepository,
                        SellerRepository sellerRepository,
                        ShipmentRepository shipmentRepository,
                        ShippingMethodRepository shippingMethodRepository,
                        ShippingPartnerRepository shippingPartnerRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        NotificationRepository notificationRepository,
                        NotificationService notificationService,
                        ObjectMapper objectMapper) {
        this.complaintRepository = complaintRepository;
        this.complaintMessageRepository = complaintMessageRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminMetricRepository = adminMetricRepository;
        this.voucherRepository = voucherRepository;
        this.productRepository = productRepository;
        this.promotionRepository = promotionRepository;
        this.sellerRepository = sellerRepository;
        this.shipmentRepository = shipmentRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.shippingPartnerRepository = shippingPartnerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    public List<AdminUserDTO> listUsers(String q, String role, String status) {
        return userRepository.findAll().stream()
            .filter(u -> q == null || q.isBlank()
                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q.toLowerCase()))
                || (u.getFullName() != null && u.getFullName().toLowerCase().contains(q.toLowerCase())))
            .filter(u -> {
                if (role == null || role.isBlank()) return true;
                try {
                    return u.getUserType() == User.UserType.valueOf(role.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            })
            .filter(u -> {
                if (status == null || status.isBlank()) return true;
                try {
                    return u.getStatus() == User.UserStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            })
            .map(this::toAdminUserDTO)
            .collect(Collectors.toList());
    }

    public AdminUserDTO createUser(CreateUserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        if (request.getUserType() != null) {
            try {
                user.setUserType(User.UserType.valueOf(request.getUserType().toUpperCase()));
            } catch (IllegalArgumentException ignored) { }
        }
        if (user.getUserType() == null) {
            user.setUserType(User.UserType.CUSTOMER);
        }
        if (request.getStatus() != null) {
            try {
                user.setStatus(User.UserStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException ignored) { }
        }
        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }
        // Generate temporary password
        String tempPassword = UUID.randomUUID().toString();
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user = userRepository.save(user);
        return toAdminUserDTO(user);
    }

    public Optional<AdminUserDTO> updateUserStatus(String id, String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        try {
            User.UserStatus newStatus = User.UserStatus.valueOf(status.toUpperCase());
            return userRepository.findById(id)
                .map(user -> {
                    user.setStatus(newStatus);
                    return userRepository.save(user);
                })
                .map(this::toAdminUserDTO);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Optional<AdminUserDTO> updateUserRole(String id, String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }
        try {
            User.UserType newRole = User.UserType.valueOf(role.toUpperCase());
            return userRepository.findById(id)
                .map(user -> {
                    user.setUserType(newRole);
                    return userRepository.save(user);
                })
                .map(this::toAdminUserDTO);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public boolean deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public List<AdminSellerDTO> listSellers(String q, String status) {
        String query = q != null && !q.isBlank() ? q.trim().toLowerCase() : null;
        Seller.VerificationStatus filterStatus = parseSellerVerificationStatus(status);

        return sellerRepository.findAll().stream()
            .filter(seller -> query == null || matchesSellerQuery(seller, query))
            .filter(seller -> filterStatus == null || seller.getVerificationStatus() == filterStatus)
            .sorted(Comparator.comparing(
                Seller::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed())
            .map(this::toAdminSellerDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public Optional<AdminSellerDTO> updateSellerStatus(String id, String status) {
        Seller.VerificationStatus newStatus = parseSellerVerificationStatus(status);
        if (newStatus == null) {
            return Optional.empty();
        }
        return sellerRepository.findById(id)
            .map(seller -> {
                seller.setVerificationStatus(newStatus);
                return sellerRepository.save(seller);
            })
            .map(this::toAdminSellerDTO);
    }

    public AdminShipmentDTO createShipment(CreateShipmentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        ShippingMethod method = resolveShippingMethod();
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setShippingMethod(method);
        if (request.getShipperId() != null && !request.getShipperId().isBlank()) {
            shipment.setShippingPartner(resolveShippingPartner(request.getShipperId()));
        }
        shipment.setTrackingNumber(generateTrackingNumber(order));
        applyAddressFromMap(shipment, request.getPickupAddress(), true);
        applyAddressFromMap(shipment, request.getDeliveryAddress(), false);
        shipment.setWeight(toBigDecimal(request.getPackageWeight()));
        shipment.setPackageSize(request.getPackageSize());
        shipment.setCodAmount(toBigDecimal(request.getCodAmount()));
        shipment.setNotes(request.getNotes());
        shipment.setStatus(parseShipmentStatus(request.getStatus()));
        shipment.setExpectedDeliveryDate(LocalDate.now().plusDays(5));
        shipment = shipmentRepository.save(shipment);
        return toAdminShipmentDTO(shipment);
    }
    
    public List<AdminShipmentDTO> listShipments(String status) {
        List<Shipment> shipments;
        if (status != null && !status.isBlank()) {
            shipments = shipmentRepository.findByStatus(parseShipmentStatus(status));
        } else {
            shipments = shipmentRepository.findAll();
        }
        Comparator<Shipment> comparator = Comparator.comparing(
            Shipment::getCreatedAt,
            Comparator.nullsLast(Comparator.naturalOrder())
        ).reversed();
        return shipments.stream()
            .sorted(comparator)
            .map(this::toAdminShipmentDTO)
            .collect(Collectors.toList());
    }
    
    public Optional<AdminShipmentDTO> assignShipment(String id, String shipperId) {
        return shipmentRepository.findById(id)
            .map(shipment -> {
                if (shipperId != null && !shipperId.isBlank()) {
                    shipment.setShippingPartner(resolveShippingPartner(shipperId));
                }
                shipment.setStatus(Shipment.ShipmentStatus.PICKED_UP);
                return shipmentRepository.save(shipment);
            })
            .map(this::toAdminShipmentDTO);
    }
    
    public Optional<AdminShipmentDTO> updateShipmentStatus(String id, String status) {
        if (status == null || status.isBlank()) {
            return Optional.empty();
        }
        Shipment.ShipmentStatus shipmentStatus = parseShipmentStatus(status);
        return shipmentRepository.findById(id)
            .map(shipment -> {
                shipment.setStatus(shipmentStatus);
                if (shipmentStatus == Shipment.ShipmentStatus.DELIVERED) {
                    shipment.setActualDeliveryDate(LocalDate.now());
                }
                return shipmentRepository.save(shipment);
            })
            .map(this::toAdminShipmentDTO);
    }
    
    public List<AdminShipmentDTO> listAvailableShipments() {
        return shipmentRepository.findByStatus(Shipment.ShipmentStatus.READY_FOR_PICKUP).stream()
            .sorted(Comparator.comparing(
                Shipment::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed())
            .map(this::toAdminShipmentDTO)
            .collect(Collectors.toList());
    }

    public List<AdminVoucherDTO> listVouchers(String q, String status, String type) {
        return voucherRepository.findAll().stream()
            .filter(v -> q == null || q.isBlank()
                || (v.getCode() != null && v.getCode().toLowerCase().contains(q.toLowerCase()))
                || (v.getDescription() != null && v.getDescription().toLowerCase().contains(q.toLowerCase())))
            .filter(v -> {
                if (status == null || status.isBlank()) return true;
                try {
                    return v.getStatus() == Voucher.VoucherStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            })
            .filter(v -> {
                if (type == null || type.isBlank()) return true;
                try {
                    return v.getDiscountType() == Voucher.DiscountType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    return false;
                }
            })
            .map(this::toAdminVoucherDTO)
            .collect(Collectors.toList());
    }

    public AdminVoucherDTO createVoucher(CreateVoucherRequest request) {
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode());
        voucher.setDescription(request.getDescription());
        voucher.setDiscountType(parseDiscountType(request.getType()));
        voucher.setDiscountValue(toBigDecimal(request.getValue()));
        voucher.setMaxDiscount(toBigDecimal(request.getMaxDiscount()));
        voucher.setMinPurchaseAmount(toBigDecimal(request.getMinOrderValue()));
        voucher.setTotalUsesLimit(request.getUsageLimit());
        voucher.setTotalUses(0);
        voucher.setStartDate(parseDateTime(request.getStartDate(), LocalDateTime.now()));
        voucher.setEndDate(parseDateTime(request.getEndDate(), LocalDateTime.now().plusMonths(1)));
        voucher.setStatus(parseVoucherStatus(request.getStatus()));
        voucher = voucherRepository.save(voucher);
        return toAdminVoucherDTO(voucher);
    }

    public Optional<AdminVoucherDTO> updateVoucher(String id, UpdateVoucherRequest request) {
        return voucherRepository.findById(id)
            .map(voucher -> {
                if (request.getCode() != null) voucher.setCode(request.getCode());
                if (request.getDescription() != null) voucher.setDescription(request.getDescription());
                if (request.getType() != null) voucher.setDiscountType(parseDiscountType(request.getType()));
                if (request.getValue() != null) voucher.setDiscountValue(toBigDecimal(request.getValue()));
                if (request.getMaxDiscount() != null) voucher.setMaxDiscount(toBigDecimal(request.getMaxDiscount()));
                if (request.getMinOrderValue() != null) voucher.setMinPurchaseAmount(toBigDecimal(request.getMinOrderValue()));
                if (request.getUsageLimit() != null) voucher.setTotalUsesLimit(request.getUsageLimit());
                if (request.getUsedCount() != null) voucher.setTotalUses(request.getUsedCount());
                if (request.getStartDate() != null) voucher.setStartDate(parseDateTime(request.getStartDate(), voucher.getStartDate()));
                if (request.getEndDate() != null) voucher.setEndDate(parseDateTime(request.getEndDate(), voucher.getEndDate()));
                if (request.getStatus() != null) voucher.setStatus(parseVoucherStatus(request.getStatus()));
                return voucherRepository.save(voucher);
            })
            .map(this::toAdminVoucherDTO);
    }

    public boolean deleteVoucher(String id) {
        if (!voucherRepository.existsById(id)) {
            return false;
        }
        voucherRepository.deleteById(id);
        return true;
    }

    public PageResponse<AdminPromotionDTO> listPromotions(String sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Promotion> promotionPage = (sellerId != null && !sellerId.isBlank())
            ? promotionRepository.findBySellerId(sellerId, pageable)
            : promotionRepository.findAll(pageable);

        List<AdminPromotionDTO> content = promotionPage.stream()
            .map(this::toAdminPromotionDTO)
            .collect(Collectors.toList());

        return new PageResponse<>(
            content,
            promotionPage.getTotalElements(),
            promotionPage.getTotalPages(),
            promotionPage.getSize(),
            promotionPage.getNumber()
        );
    }

    public AdminPromotionDTO createPromotion(String sellerId, CreatePromotionRequest request) {
        Promotion promotion = new Promotion();
        promotion.setSeller(sellerRepository.findById(sellerId)
            .orElseThrow(() -> new IllegalArgumentException("Seller not found")));
        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setPromotionType(parsePromotionType(request.getPromotionType()));
        promotion.setDiscountValue(toBigDecimal(request.getDiscountValue()));
        promotion.setStartDate(parseDateTime(request.getStartDate(), LocalDateTime.now()));
        promotion.setEndDate(parseDateTime(request.getEndDate(), LocalDateTime.now().plusMonths(1)));
        promotion.setStatus(parsePromotionStatus(request.getStatus()));
        promotion.setQuantityLimit(request.getQuantityLimit());
        promotion.setQuantityUsed(0);

        promotion = promotionRepository.save(promotion);
        return toAdminPromotionDTO(promotion);
    }

    public List<AdminComplaintDTO> listComplaints(String status) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Complaint> complaints = status == null || status.isEmpty()
            ? complaintRepository.findAll(sort)
            : complaintRepository.findByStatus(status, sort);
        return complaints.stream()
            .map(this::toComplaintDTO)
            .collect(Collectors.toList());
    }

    public List<AdminComplaintDTO> listComplaintsByReporter(String reporterId, String status) {
        if (reporterId == null || reporterId.isEmpty()) {
            return List.of();
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Complaint> complaints = status == null || status.isEmpty()
            ? complaintRepository.findByReporterId(reporterId, sort)
            : complaintRepository.findByReporterIdAndStatus(reporterId, status, sort);
        return complaints.stream()
            .map(this::toComplaintDTO)
            .collect(Collectors.toList());
    }

    public List<AdminComplaintDTO> listComplaintsByTarget(String targetId, String status) {
        if (targetId == null || targetId.isEmpty()) {
            return List.of();
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        List<Complaint> complaints = status == null || status.isEmpty()
            ? complaintRepository.findByTargetId(targetId, sort)
            : complaintRepository.findByTargetIdAndStatus(targetId, status, sort);
        return complaints.stream()
            .map(this::toComplaintDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminComplaintDTO createComplaint(CreateComplaintRequest request) {
        String reporterId = request.getReporterId();
        String orderId = request.getOrderId();
        String productId = request.getProductId();

        // Validate: Customer must have purchased the product/order to file a complaint
        if (orderId != null && !orderId.isEmpty()) {
            // Check if customer owns the order
            if (!orderRepository.existsByIdAndCustomerId(orderId, reporterId)) {
                throw new com.shopcuathuy.exception.ForbiddenException(
                    "Bạn không có quyền khiếu nại đơn hàng này. Chỉ khách hàng đã mua đơn hàng mới có thể khiếu nại.");
            }
        } else if (productId != null && !productId.isEmpty()) {
            // Check if customer has purchased the product
            if (!orderItemRepository.existsByProductIdAndCustomerId(productId, reporterId)) {
                throw new com.shopcuathuy.exception.ForbiddenException(
                    "Bạn không có quyền khiếu nại sản phẩm này. Chỉ khách hàng đã mua sản phẩm mới có thể khiếu nại.");
            }
        } else {
            // If neither orderId nor productId is provided, allow general complaint
            // (e.g., complaint about seller service, shipping, etc.)
        }

        Complaint complaint = new Complaint();
        complaint.setReporterId(reporterId);
        complaint.setTargetId(request.getTargetId());
        complaint.setCategory(request.getCategory());
        complaint.setTitle(request.getTitle());
        complaint.setContent(request.getContent());
        complaint.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        complaint.setOrderId(orderId);
        complaint.setProductId(productId);
        complaint.setDesiredResolution(request.getDesiredResolution());
        complaint.setAttachments(serializeAttachments(request.getAttachments()));
        complaint.setDueAt(LocalDateTime.now().plusHours(24));

        Complaint saved = complaintRepository.save(complaint);
        AdminComplaintDTO dto = toComplaintDTO(saved);
        notifySellerOfComplaint(saved, "Yêu cầu khiếu nại mới", saved.getTitle());
        return dto;
    }

    @Transactional
    public Optional<AdminComplaintDTO> updateComplaintStatus(String id, String status) {
        if (status == null || status.isEmpty()) {
            return Optional.empty();
        }
        return complaintRepository.findById(id)
            .map(complaint -> {
                complaint.setStatus(status);
                if ("RESOLVED".equalsIgnoreCase(status)) {
                    complaint.setResolvedAt(LocalDateTime.now());
                }
                return complaintRepository.save(complaint);
            })
            .map(saved -> {
                AdminComplaintDTO dto = toComplaintDTO(saved);
                notifyCustomerOfComplaint(saved, "Khiếu nại cập nhật", "Trạng thái: " + status);
                notifySellerOfComplaint(saved, "Khiếu nại cập nhật", "Trạng thái: " + status);
                return dto;
            });
    }

    @Transactional
    public Optional<AdminComplaintDTO> cancelComplaint(String id, String reporterId) {
        if (id == null || id.isEmpty() || reporterId == null || reporterId.isEmpty()) {
            return Optional.empty();
        }
        return complaintRepository.findById(id)
            .filter(complaint -> reporterId.equals(complaint.getReporterId()))
            .map(complaint -> {
                complaint.setStatus("CANCELLED");
                complaint.setResolvedAt(LocalDateTime.now());
                return complaintRepository.save(complaint);
            })
            .map(saved -> {
                AdminComplaintDTO dto = toComplaintDTO(saved);
                notifySellerOfComplaint(saved, "Khách đã hủy khiếu nại", saved.getTitle());
                return dto;
            });
    }

    @Transactional(readOnly = true)
    public List<ComplaintMessageResponseDTO> listComplaintMessages(String complaintId) {
        if (complaintId == null || complaintId.isEmpty()) {
            return List.of();
        }
        return complaintMessageRepository.findByComplaintIdOrderByCreatedAtAsc(complaintId).stream()
            .map(this::toComplaintMessageDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public Optional<ComplaintMessageResponseDTO> addComplaintMessage(String complaintId,
                                                                     String senderId,
                                                                     String senderType,
                                                                     String content,
                                                                     List<String> attachments) {
        if (complaintId == null || complaintId.isEmpty() || senderId == null
            || senderId.isEmpty() || content == null || content.trim().isEmpty()) {
            return Optional.empty();
        }
        return complaintRepository.findById(complaintId)
            .map(complaint -> {
                ComplaintMessage message = new ComplaintMessage();
                message.setComplaint(complaint);
                message.setSenderId(senderId);
                message.setSenderType(senderType != null ? senderType : "CUSTOMER");
                message.setContent(content.trim());
                message.setAttachments(serializeAttachments(attachments));
                ComplaintMessage savedMessage = complaintMessageRepository.save(message);
                if (!"CUSTOMER".equalsIgnoreCase(message.getSenderType()) && complaint.getFirstResponseAt() == null) {
                    complaint.setFirstResponseAt(LocalDateTime.now());
                    complaintRepository.save(complaint);
                }
                if ("SELLER".equalsIgnoreCase(message.getSenderType())) {
                    notifyCustomerOfComplaint(complaint, "Seller phản hồi khiếu nại", message.getContent());
                } else if ("ADMIN".equalsIgnoreCase(message.getSenderType())) {
                    notifyCustomerOfComplaint(complaint, "Admin phản hồi khiếu nại", message.getContent());
                    notifySellerOfComplaint(complaint, "Admin phản hồi khiếu nại", message.getContent());
                } else {
                    notifySellerOfComplaint(complaint, "Khách phản hồi khiếu nại", message.getContent());
                }
                return savedMessage;
            })
            .map(this::toComplaintMessageDTO);
    }

    public Optional<AdminComplaintDTO> getComplaint(String id) {
        return complaintRepository.findById(id).map(this::toComplaintDTO);
    }

    public Optional<AdminComplaintDTO> getComplaintForSeller(String id, String sellerId) {
        if (sellerId == null || sellerId.isEmpty()) {
            return Optional.empty();
        }
        return complaintRepository.findById(id)
            .filter(complaint -> sellerId.equals(complaint.getTargetId()))
            .map(this::toComplaintDTO);
    }

    public AdminSystemMetricsDTO getMetrics() {
        AdminMetric metric = getOrCreateMetric();
        AdminSystemMetricsDTO dto = new AdminSystemMetricsDTO();
        dto.setStartedAt(metric.getCreatedAt() != null
            ? metric.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : Instant.now());
        dto.setRequestCount(metric.getRequestCount());
        dto.setErrorCount(metric.getRequestCount() - metric.getSuccessCount());
        dto.setAvgResponseMs(metric.getAvgResponseMs() != null ? metric.getAvgResponseMs() : 0L);
        return dto;
    }

    public void recordRequest(boolean success) {
        AdminMetric metric = getOrCreateMetric();
        metric.setRequestCount(metric.getRequestCount() + 1);
        if (success) {
            metric.setSuccessCount(metric.getSuccessCount() + 1);
        }
        adminMetricRepository.save(metric);
    }

    @Transactional(readOnly = true)
    public AdminOverviewDTO getSystemOverview(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusSeconds(1);

        List<Order> orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
        double revenue = orders.stream()
            .filter(order -> order.getFinalTotal() != null)
            .mapToDouble(order -> order.getFinalTotal().doubleValue())
            .sum();
        long totalOrders = orders.size();
        long totalCustomers = orders.stream()
            .map(Order::getCustomer)
            .filter(Objects::nonNull)
            .map(User::getId)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        long activeSellers = sellerRepository.count();

        AdminOverviewDTO dto = new AdminOverviewDTO();
        dto.setStartDate(start);
        dto.setEndDate(end);
        dto.setTotalRevenue(revenue);
        dto.setTotalOrders(totalOrders);
        dto.setTotalCustomers(totalCustomers);
        dto.setActiveSellers(activeSellers);
        dto.setTopSellers(buildTopSellerStats(orders));
        return dto;
    }

    @Transactional(readOnly = true)
    public SellerOverviewDTO getSellerOverview(String sellerId) {
        String targetSellerId = resolveSellerId(sellerId);
        if (targetSellerId == null) {
            return new SellerOverviewDTO(0, 0, 0, 0.0);
        }

        long productCount = productRepository.countBySellerIdAndStatus(targetSellerId, Product.ProductStatus.ACTIVE);
        List<Order> sellerOrders = getOrdersForSeller(targetSellerId);
        long totalOrders = sellerOrders.size();
        long totalCustomers = sellerOrders.stream()
            .map(order -> order.getCustomer() != null ? order.getCustomer().getId() : null)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        double totalRevenue = calculateRevenue(targetSellerId);

        return new SellerOverviewDTO(productCount, totalOrders, totalCustomers, totalRevenue);
    }


    public PageResponse<NotificationDTO> listNotifications(String userId, int page, int size) {
        if (userId == null || userId.isBlank()) {
            return new PageResponse<>(List.of(), 0, 0, size, page);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notificationPage =
            notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationDTO> content = notificationPage.stream()
            .map(this::toNotificationDTO)
            .collect(Collectors.toList());
        return new PageResponse<>(
            content,
            notificationPage.getTotalElements(),
            notificationPage.getTotalPages(),
            notificationPage.getSize(),
            notificationPage.getNumber()
        );
    }

    public long getUnreadCount(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0L;
        }
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public Optional<NotificationDTO> markNotificationAsRead(String userId, String notificationId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return notificationRepository.findById(notificationId)
            .filter(notification -> notification.getRecipient() != null
                && userId.equals(notification.getRecipient().getId()))
            .map(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
                return notificationRepository.save(notification);
            })
            .map(this::toNotificationDTO);
    }

    @Transactional
    public void markAllNotificationsAsRead(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        List<Notification> notifications = notificationRepository.findByRecipientId(userId);
        notifications.stream()
            .filter(notification -> !Boolean.TRUE.equals(notification.getIsRead()))
            .forEach(notification -> {
                notification.setIsRead(true);
                notification.setReadAt(LocalDateTime.now());
            });
        notificationRepository.saveAll(notifications);
    }

    public boolean deleteNotification(String userId, String notificationId) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        return notificationRepository.findById(notificationId)
            .filter(notification -> notification.getRecipient() != null
                && userId.equals(notification.getRecipient().getId()))
            .map(notification -> {
                notificationRepository.delete(notification);
                return true;
            })
            .orElse(false);
    }

    @Transactional(readOnly = true)
    public SellerAnalyticsDashboardDTO getAnalyticsDashboard(String period) {
        SellerAnalyticsDashboardDTO dto = new SellerAnalyticsDashboardDTO();

        SellerAnalyticsOverviewDTO overview = new SellerAnalyticsOverviewDTO();
        double revenue = calculateRevenue(null);
        long orders = orderRepository.count();
        overview.setRevenue(revenue);
        overview.setRevenueChange(estimateChange(revenue));
        overview.setOrders(orders);
        overview.setOrdersChange(estimateChange(orders));
        overview.setAverageOrderValue(orders > 0 ? revenue / orders : 0.0);
        overview.setAverageOrderValueChange(estimateChange(overview.getAverageOrderValue()));
        overview.setConversionRate(estimateConversionRate());
        overview.setConversionRateChange(estimateChange(overview.getConversionRate()));
        dto.setOverview(overview);

        dto.setRevenueSeries(calculateRevenueSeries(period));
        dto.setCategorySeries(calculateCategorySeries());
        dto.setCustomerTypes(calculateCustomerTypes());
        dto.setCustomerLocations(calculateCustomerLocations());
        dto.setTrafficSeries(calculateTrafficSeries(period));
        dto.setTrafficSources(calculateTrafficSources());
        dto.setTopProducts(calculateTopProducts());
        dto.setLowStockProducts(calculateLowStockProducts());

        return dto;
    }

    private AdminComplaintDTO toComplaintDTO(Complaint complaint) {
        AdminComplaintDTO dto = new AdminComplaintDTO();
        dto.setId(complaint.getId());
        dto.setReporterId(complaint.getReporterId());
        dto.setTargetId(complaint.getTargetId());
        dto.setCategory(complaint.getCategory());
        dto.setTitle(complaint.getTitle());
        dto.setContent(complaint.getContent());
        dto.setStatus(complaint.getStatus());
        dto.setOrderId(complaint.getOrderId());
        dto.setProductId(complaint.getProductId());
        dto.setDesiredResolution(complaint.getDesiredResolution());
        dto.setAttachments(deserializeAttachments(complaint.getAttachments()));
        if (complaint.getCreatedAt() != null) {
            dto.setCreatedAt(complaint.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        }
        if (complaint.getUpdatedAt() != null) {
            dto.setUpdatedAt(complaint.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant());
        }
        dto.setDueAt(toInstant(complaint.getDueAt()));
        dto.setFirstResponseAt(toInstant(complaint.getFirstResponseAt()));
        dto.setResolvedAt(toInstant(complaint.getResolvedAt()));
        boolean closed = complaint.getStatus() != null
            && (complaint.getStatus().equalsIgnoreCase("RESOLVED")
                || complaint.getStatus().equalsIgnoreCase("REJECTED")
                || complaint.getStatus().equalsIgnoreCase("CANCELLED"));
        if (complaint.getDueAt() != null) {
            dto.setOverdue(!closed && LocalDateTime.now().isAfter(complaint.getDueAt()));
        } else {
            dto.setOverdue(false);
        }
        if (complaint.getCreatedAt() != null && complaint.getFirstResponseAt() != null) {
            dto.setFirstResponseMinutes(Duration.between(complaint.getCreatedAt(), complaint.getFirstResponseAt()).toMinutes());
        }
        if (complaint.getCreatedAt() != null && complaint.getResolvedAt() != null) {
            dto.setResolutionMinutes(Duration.between(complaint.getCreatedAt(), complaint.getResolvedAt()).toMinutes());
        }
        return dto;
    }

    private ComplaintMessageResponseDTO toComplaintMessageDTO(ComplaintMessage message) {
        ComplaintMessageResponseDTO dto = new ComplaintMessageResponseDTO();
        dto.id = message.getId();
        dto.complaintId = message.getComplaint() != null ? message.getComplaint().getId() : null;
        dto.senderId = message.getSenderId();
        dto.senderType = message.getSenderType();
        dto.content = message.getContent();
        dto.attachments = deserializeAttachments(message.getAttachments());
        if (message.getCreatedAt() != null) {
            dto.createdAt = message.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
        }
        return dto;
    }

    private Instant toInstant(LocalDateTime value) {
        return value != null ? value.atZone(ZoneId.systemDefault()).toInstant() : null;
    }

    private String serializeAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            return String.join(",", attachments);
        }
    }

    private List<String> deserializeAttachments(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {});
        } catch (Exception ex) {
            return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
    }

    private void notifyCustomerOfComplaint(Complaint complaint, String title, String message) {
        notifyComplaintEvent(complaint.getReporterId(), title, message, complaint.getId(),
            "/complaints/" + complaint.getId());
    }

    private void notifySellerOfComplaint(Complaint complaint, String title, String message) {
        if (complaint.getTargetId() == null || complaint.getTargetId().isBlank()) {
            return;
        }
        boolean sent = sellerRepository.findById(complaint.getTargetId())
            .map(Seller::getUser)
            .map(User::getId)
            .map(userId -> {
                notifyComplaintEvent(userId, title, message, complaint.getId(),
                    "/seller/complaints/" + complaint.getId());
                return true;
            })
            .orElse(false);
        if (!sent) {
            notifyComplaintEvent(complaint.getTargetId(), title, message, complaint.getId(),
                "/seller/complaints/" + complaint.getId());
        }
    }

    private void notifyComplaintEvent(String recipientId, String title, String message, String complaintId, String linkUrl) {
        if (recipientId == null || recipientId.isBlank()) {
            return;
        }
        userRepository.findById(recipientId).ifPresent(user ->
            notificationService.createAndDispatch(
                user,
                Notification.NotificationType.COMPLAINT,
                title,
                message,
                linkUrl,
                complaintId,
                null
            )
        );
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setUserType(user.getUserType() != null ? user.getUserType().name() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        }
        return dto;
    }

    private AdminMetric getOrCreateMetric() {
        return adminMetricRepository.findAll().stream().findFirst()
            .orElseGet(() -> adminMetricRepository.save(new AdminMetric()));
    }

    private AdminVoucherDTO toAdminVoucherDTO(Voucher voucher) {
        AdminVoucherDTO dto = new AdminVoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setType(voucher.getDiscountType() != null ? voucher.getDiscountType().name() : null);
        dto.setValue(voucher.getDiscountValue() != null ? voucher.getDiscountValue().doubleValue() : null);
        dto.setMaxDiscount(voucher.getMaxDiscount() != null ? voucher.getMaxDiscount().doubleValue() : null);
        dto.setMinOrderValue(voucher.getMinPurchaseAmount() != null ? voucher.getMinPurchaseAmount().doubleValue() : null);
        dto.setUsageLimit(voucher.getTotalUsesLimit());
        dto.setUsedCount(voucher.getTotalUses());
        dto.setStartDate(voucher.getStartDate() != null ? voucher.getStartDate().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setEndDate(voucher.getEndDate() != null ? voucher.getEndDate().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setStatus(voucher.getStatus() != null ? voucher.getStatus().name() : null);
        dto.setCreatedAt(voucher.getCreatedAt() != null ? voucher.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setUpdatedAt(voucher.getUpdatedAt() != null ? voucher.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        return dto;
    }

    private NotificationDTO toNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getRecipient() != null ? notification.getRecipient().getId() : null);
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType() != null ? notification.getType().name() : null);
        dto.setLinkUrl(notification.getLinkUrl());
        dto.setImageUrl(notification.getImageUrl());
        dto.setRead(Boolean.TRUE.equals(notification.getIsRead()));
        dto.setCreatedAt(notification.getCreatedAt() != null
            ? notification.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : null);
        return dto;
    }

    private AdminPromotionDTO toAdminPromotionDTO(Promotion promotion) {
        AdminPromotionDTO dto = new AdminPromotionDTO();
        dto.setId(promotion.getId());
        dto.setSellerId(promotion.getSeller() != null ? promotion.getSeller().getId() : null);
        dto.setName(promotion.getName());
        dto.setDescription(promotion.getDescription());
        dto.setPromotionType(promotion.getPromotionType() != null ? promotion.getPromotionType().name() : null);
        dto.setDiscountValue(promotion.getDiscountValue() != null ? promotion.getDiscountValue().doubleValue() : null);
        dto.setStartDate(promotion.getStartDate() != null ? promotion.getStartDate().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setEndDate(promotion.getEndDate() != null ? promotion.getEndDate().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setStatus(promotion.getStatus() != null ? promotion.getStatus().name() : null);
        dto.setQuantityLimit(promotion.getQuantityLimit());
        dto.setQuantityUsed(promotion.getQuantityUsed());
        return dto;
    }

    private AdminShipmentDTO toAdminShipmentDTO(Shipment shipment) {
        AdminShipmentDTO dto = new AdminShipmentDTO();
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrder() != null ? shipment.getOrder().getId() : null);
        dto.setSellerId(
            shipment.getOrder() != null && shipment.getOrder().getSeller() != null
                ? shipment.getOrder().getSeller().getId()
                : null
        );
        dto.setShipperId(shipment.getShippingPartner() != null ? shipment.getShippingPartner().getId() : null);
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setStatus(shipment.getStatus() != null ? shipment.getStatus().name() : null);
        dto.setPickupAddress(buildAddressMap(
            shipment.getSenderName(),
            shipment.getSenderPhone(),
            shipment.getSenderAddress(),
            shipment.getSenderProvince(),
            shipment.getSenderDistrict(),
            shipment.getSenderWard()
        ));
        dto.setDeliveryAddress(buildAddressMap(
            shipment.getRecipientName(),
            shipment.getRecipientPhone(),
            shipment.getRecipientAddress(),
            shipment.getRecipientProvince(),
            shipment.getRecipientDistrict(),
            shipment.getRecipientWard()
        ));
        dto.setPackageWeight(shipment.getWeight() != null ? shipment.getWeight().doubleValue() : null);
        dto.setPackageSize(shipment.getPackageSize());
        dto.setCodAmount(shipment.getCodAmount() != null ? shipment.getCodAmount().doubleValue() : null);
        dto.setNotes(shipment.getNotes());
        dto.setCreatedAt(shipment.getCreatedAt() != null
            ? shipment.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : null);
        dto.setUpdatedAt(shipment.getUpdatedAt() != null
            ? shipment.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : null);
        return dto;
    }

    private AdminSellerDTO toAdminSellerDTO(Seller seller) {
        AdminSellerDTO dto = new AdminSellerDTO();
        dto.setId(seller.getId());
        dto.setUserId(seller.getUser() != null ? seller.getUser().getId() : null);
        dto.setShopName(seller.getShopName());
        String slug = slugify(seller.getShopName());
        dto.setSlug(slug != null ? slug : seller.getId());
        dto.setStatus(seller.getVerificationStatus() != null
            ? seller.getVerificationStatus().name()
            : Seller.VerificationStatus.UNVERIFIED.name());
        dto.setCreatedAt(seller.getCreatedAt() != null
            ? seller.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
            : Instant.now());
        return dto;
    }

    private boolean matchesSellerQuery(Seller seller, String query) {
        if (query == null) {
            return true;
        }
        if (seller.getShopName() != null
            && seller.getShopName().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        if (seller.getUser() != null) {
            if (seller.getUser().getEmail() != null
                && seller.getUser().getEmail().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
            if (seller.getUser().getFullName() != null
                && seller.getUser().getFullName().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
            if (seller.getUser().getId() != null
                && seller.getUser().getId().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }

    private Seller.VerificationStatus parseSellerVerificationStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("APPROVED".equals(normalized)) {
            normalized = "VERIFIED";
        } else if ("REVIEW".equals(normalized)) {
            normalized = "PENDING";
        }
        try {
            return Seller.VerificationStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String slugify(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-|-$", "");
        return normalized.isBlank() ? null : normalized;
    }

    private String resolveSellerId(String sellerId) {
        if (sellerId != null && !sellerId.isBlank()) {
            return sellerId;
        }
        return sellerRepository.findAll().stream()
            .findFirst()
            .map(seller -> seller.getId())
            .orElse(null);
    }

    private double calculateRevenue(String sellerId) {
        return getOrdersForSeller(sellerId).stream()
            .map(order -> order.getFinalTotal() != null ? order.getFinalTotal().doubleValue() : 0.0)
            .reduce(0.0, Double::sum);
    }

    private List<Order> getOrdersForSeller(String sellerId) {
        List<Order> orders = orderRepository.findAll();
        if (sellerId == null || sellerId.isBlank()) {
            return orders;
        }
        return orders.stream()
            .filter(order -> order.getSeller() != null && sellerId.equals(order.getSeller().getId()))
            .collect(Collectors.toList());
    }

    private List<AdminOverviewDTO.TopSellerDTO> buildTopSellerStats(List<Order> orders) {
        Map<String, SellerAggregate> aggregates = new HashMap<>();
        for (Order order : orders) {
            if (order.getSeller() == null) {
                continue;
            }
            String sellerId = order.getSeller().getId();
            SellerAggregate aggregate = aggregates.computeIfAbsent(sellerId, id -> new SellerAggregate(order.getSeller()));
            aggregate.incrementOrders();
            aggregate.addRevenue(order.getFinalTotal());
        }
        return aggregates.values().stream()
            .sorted(Comparator.comparing(SellerAggregate::getRevenue).reversed())
            .limit(5)
            .map(aggregate -> {
                AdminOverviewDTO.TopSellerDTO dto = new AdminOverviewDTO.TopSellerDTO();
                dto.setSellerId(aggregate.getSellerId());
                dto.setShopName(aggregate.getShopName());
                dto.setOrders(aggregate.getOrders());
                dto.setRevenue(aggregate.getRevenue());
                return dto;
            })
            .collect(Collectors.toList());
    }

    private List<Order> filterOrdersByPeriod(String period) {
        LocalDate start = resolvePeriodStart(period);
        return orderRepository.findAll().stream()
            .filter(order -> order.getCreatedAt() != null)
            .filter(order -> !order.getCreatedAt().toLocalDate().isBefore(start))
            .collect(Collectors.toList());
    }

    private LocalDate resolvePeriodStart(String period) {
        LocalDate today = LocalDate.now();
        if (period == null || period.isBlank()) {
            return today.minusDays(30);
        }
        return switch (period.toLowerCase()) {
            case "7days", "7d" -> today.minusDays(7);
            case "90days", "3months", "90d" -> today.minusDays(90);
            case "1year", "365d" -> today.minusDays(365);
            default -> today.minusDays(30);
        };
    }

    private List<RevenuePointDTO> calculateRevenueSeries(String period) {
        Map<LocalDate, List<Order>> grouped = filterOrdersByPeriod(period).stream()
            .collect(Collectors.groupingBy(order -> order.getCreatedAt() != null
                ? order.getCreatedAt().toLocalDate()
                : LocalDate.now()));
        LocalDate start = resolvePeriodStart(period);
        LocalDate end = LocalDate.now();
        List<RevenuePointDTO> points = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            List<Order> orders = grouped.getOrDefault(date, List.of());
            double revenue = orders.stream()
                .map(order -> order.getFinalTotal() != null ? order.getFinalTotal().doubleValue() : 0.0)
                .reduce(0.0, Double::sum);
            int orderCount = orders.size();
            double profit = revenue * 0.25;
            points.add(new RevenuePointDTO(date.toString(), revenue, profit, orderCount));
        }
        return points;
    }

    private List<CategoryRevenueDTO> calculateCategorySeries() {
        Map<String, Double> totals = new LinkedHashMap<>();
        orderItemRepository.findAll().forEach(item -> {
            Product product = item.getProduct();
            String categoryName = (product != null && product.getCategory() != null)
                ? product.getCategory().getName()
                : "Khác";
            double value = item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : 0.0;
            totals.merge(categoryName, value, Double::sum);
        });
        return totals.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(entry -> new CategoryRevenueDTO(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private List<CustomerTypeDTO> calculateCustomerTypes() {
        Map<String, Long> ordersByCustomer = orderRepository.findAll().stream()
            .filter(order -> order.getCustomer() != null)
            .collect(Collectors.groupingBy(order -> order.getCustomer().getId(), Collectors.counting()));
        long newCustomers = ordersByCustomer.values().stream().filter(count -> count == 1).count();
        long returningCustomers = ordersByCustomer.values().stream().filter(count -> count > 1).count();
        long totalCustomers = ordersByCustomer.size();
        long inactiveCustomers = Math.max(0, totalCustomers - newCustomers - returningCustomers);

        CustomerTypeDTO newDto = new CustomerTypeDTO("Khách mới", newCustomers, "#38bdf8");
        CustomerTypeDTO returningDto = new CustomerTypeDTO("Khách quay lại", returningCustomers, "#0ea5e9");
        CustomerTypeDTO inactiveDto = new CustomerTypeDTO("Khách không hoạt động", inactiveCustomers, "#e2e8f0");
        return List.of(newDto, returningDto, inactiveDto);
    }

    private List<CustomerLocationDTO> calculateCustomerLocations() {
        Map<String, Long> locations = new HashMap<>();
        orderRepository.findAll().forEach(order -> {
            String province = "Không rõ";
            if (order.getCustomer() != null && order.getCustomer().getAddresses() != null
                && !order.getCustomer().getAddresses().isEmpty()) {
                province = Optional.ofNullable(order.getCustomer().getAddresses().get(0).getProvince())
                    .filter(value -> !value.isBlank())
                    .orElse("Không rõ");
            }
            locations.merge(province, 1L, Long::sum);
        });

        return locations.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(entry -> new CustomerLocationDTO(entry.getKey(), entry.getValue().intValue()))
            .collect(Collectors.toList());
    }

    private List<TrafficPointDTO> calculateTrafficSeries(String period) {
        Map<LocalDate, Long> ordersPerDay = filterOrdersByPeriod(period).stream()
            .collect(Collectors.groupingBy(order -> order.getCreatedAt() != null
                    ? order.getCreatedAt().toLocalDate()
                    : LocalDate.now(),
                Collectors.counting()));

        LocalDate start = resolvePeriodStart(period);
        LocalDate end = LocalDate.now();
        List<TrafficPointDTO> points = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            long orderCount = ordersPerDay.getOrDefault(date, 0L);
            int visitors = (int) Math.max(orderCount * 2, 1);
            int views = visitors * 3;
            double bounceRate = 60 - Math.min(orderCount * 2, 40);
            points.add(new TrafficPointDTO(date.toString(), views, visitors, bounceRate));
        }
        return points;
    }

    private List<TrafficSourceDTO> calculateTrafficSources() {
        Map<String, Long> sources = orderRepository.findAll().stream()
            .collect(Collectors.groupingBy(order -> {
                if (order.getPaymentMethod() == null || order.getPaymentMethod().isBlank()) {
                    return "other";
                }
                return order.getPaymentMethod().toLowerCase();
            }, Collectors.counting()));

        if (sources.isEmpty()) {
            sources.put("other", 0L);
        }

        return sources.entrySet().stream()
            .map(entry -> new TrafficSourceDTO(entry.getKey(), entry.getValue().intValue()))
            .collect(Collectors.toList());
    }

    private List<TopProductDTO> calculateTopProducts() {
        Map<String, ProductSalesStats> stats = new HashMap<>();
        orderItemRepository.findAll().forEach(item -> {
            Product product = item.getProduct();
            if (product == null) {
                return;
            }
            ProductSalesStats acc = stats.computeIfAbsent(product.getId(), id -> new ProductSalesStats(product));
            int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
            double revenue = item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : 0.0;
            acc.addQuantity(quantity);
            acc.addRevenue(revenue);
        });

        return stats.values().stream()
            .sorted(Comparator.comparing(ProductSalesStats::getRevenue).reversed())
            .limit(5)
            .map(stat -> {
                TopProductDTO dto = new TopProductDTO();
                dto.setId(stat.getProduct().getId());
                dto.setName(stat.getProduct().getName());
                dto.setSold(stat.getQuantity());
                dto.setRevenue(stat.getRevenue());
                dto.setTrend(stat.getQuantity() > 0 ? "+15%" : "0%");
                dto.setTrendUp(stat.getQuantity() > 0);
                dto.setImage(stat.getProduct().getImages() != null && !stat.getProduct().getImages().isEmpty()
                    ? stat.getProduct().getImages().get(0).getImageUrl()
                    : null);
                return dto;
            })
            .collect(Collectors.toList());
    }

    private List<LowStockProductDTO> calculateLowStockProducts() {
        return productRepository.findAll().stream()
            .filter(product -> product.getQuantity() != null && product.getQuantity() < 20)
            .map(product -> new LowStockProductDTO(
                product.getName(),
                product.getQuantity(),
                product.getQuantity() != null && product.getQuantity() <= 0 ? "danger" : "warning"
            ))
            .collect(Collectors.toList());
    }

    private double estimateChange(double value) {
        if (value <= 0) {
            return 0.0;
        }
        return Math.min(25.0, Math.round((value % 100) / 4.0 * 10.0) / 10.0);
    }

    private double estimateChange(long value) {
        return estimateChange((double) value);
    }

    private double estimateConversionRate() {
        List<Order> orders = orderRepository.findAll();
        long uniqueCustomers = orders.stream()
            .map(order -> order.getCustomer() != null ? order.getCustomer().getId() : null)
            .filter(Objects::nonNull)
            .distinct()
            .count();
        long estimatedVisitors = Math.max(uniqueCustomers * 5, 1);
        return Math.min(100.0, (orders.size() * 100.0) / estimatedVisitors);
    }

    private static class ProductSalesStats {
        private final Product product;
        private int quantity;
        private double revenue;

        ProductSalesStats(Product product) {
            this.product = product;
        }

        void addQuantity(int value) {
            this.quantity += value;
        }

        void addRevenue(double value) {
            this.revenue += value;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    private static class SellerAggregate {
        private final String sellerId;
        private final String shopName;
        private long orders;
        private double revenue;

        SellerAggregate(Seller seller) {
            this.sellerId = seller != null ? seller.getId() : null;
            this.shopName = seller != null && seller.getShopName() != null
                ? seller.getShopName()
                : "Unknown seller";
        }

        void incrementOrders() {
            this.orders++;
        }

        void addRevenue(BigDecimal value) {
            if (value != null) {
                this.revenue += value.doubleValue();
            }
        }

        public String getSellerId() {
            return sellerId;
        }

        public String getShopName() {
            return shopName;
        }

        public long getOrders() {
            return orders;
        }

        public double getRevenue() {
            return revenue;
        }
    }

    private Voucher.DiscountType parseDiscountType(String type) {
        if (type == null || type.isBlank()) {
            return Voucher.DiscountType.PERCENTAGE;
        }
        try {
            return Voucher.DiscountType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Voucher.DiscountType.PERCENTAGE;
        }
    }

    private Voucher.VoucherStatus parseVoucherStatus(String status) {
        if (status == null || status.isBlank()) {
            return Voucher.VoucherStatus.ACTIVE;
        }
        try {
            return Voucher.VoucherStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Voucher.VoucherStatus.ACTIVE;
        }
    }

    private Shipment.ShipmentStatus parseShipmentStatus(String status) {
        if (status == null || status.isBlank()) {
            return Shipment.ShipmentStatus.PENDING;
        }
        try {
            return Shipment.ShipmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Shipment.ShipmentStatus.PENDING;
        }
    }

    private Promotion.PromotionType parsePromotionType(String type) {
        if (type == null || type.isBlank()) {
            return Promotion.PromotionType.PERCENTAGE;
        }
        try {
            return Promotion.PromotionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Promotion.PromotionType.PERCENTAGE;
        }
    }

    private Promotion.PromotionStatus parsePromotionStatus(String status) {
        if (status == null || status.isBlank()) {
            return Promotion.PromotionStatus.SCHEDULED;
        }
        try {
            return Promotion.PromotionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Promotion.PromotionStatus.SCHEDULED;
        }
    }

    private LocalDateTime parseDateTime(String value, LocalDateTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private ShippingMethod resolveShippingMethod() {
        return shippingMethodRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No shipping method configured"));
    }

    private ShippingPartner resolveShippingPartner(String shipperId) {
        return shippingPartnerRepository.findById(shipperId)
            .orElseThrow(() -> new IllegalArgumentException("Shipping partner not found"));
    }

    private void applyAddressFromMap(Shipment shipment, Map<String, Object> address, boolean isPickup) {
        if (address == null || address.isEmpty()) {
            return;
        }
        String name = getAddressValue(address, "name");
        String phone = getAddressValue(address, "phone");
        String addr = getAddressValue(address, "address");
        String province = getAddressValue(address, "province");
        String district = getAddressValue(address, "district");
        String ward = getAddressValue(address, "ward");
        if (isPickup) {
            shipment.setSenderName(name);
            shipment.setSenderPhone(phone);
            shipment.setSenderAddress(addr);
            shipment.setSenderProvince(province);
            shipment.setSenderDistrict(district);
            shipment.setSenderWard(ward);
        } else {
            shipment.setRecipientName(name);
            shipment.setRecipientPhone(phone);
            shipment.setRecipientAddress(addr);
            shipment.setRecipientProvince(province);
            shipment.setRecipientDistrict(district);
            shipment.setRecipientWard(ward);
        }
    }

    private Map<String, Object> buildAddressMap(String name, String phone, String address,
                                                String province, String district, String ward) {
        Map<String, Object> map = new HashMap<>();
        if (name != null) map.put("name", name);
        if (phone != null) map.put("phone", phone);
        if (address != null) map.put("address", address);
        if (province != null) map.put("province", province);
        if (district != null) map.put("district", district);
        if (ward != null) map.put("ward", ward);
        return map;
    }

    private String getAddressValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String generateTrackingNumber(Order order) {
        return "TRK-" + (order.getOrderNumber() != null ? order.getOrderNumber() : UUID.randomUUID().toString())
            + "-" + System.currentTimeMillis();
    }
}

