package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.SellerRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seller/customers")
public class SellerCustomerController {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;

    public SellerCustomerController(SellerRepository sellerRepository,
                                    OrderRepository orderRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CustomerPage>> listCustomers(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        List<Object[]> stats = orderRepository.findCustomerStats(seller.getId());
        List<SellerCustomerDTO> customers = stats.stream()
            .map(this::mapToCustomerDTO)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));

        if (StringUtils.hasText(search)) {
            String term = search.trim().toLowerCase(Locale.ROOT);
            customers = customers.stream()
                .filter(dto -> matchesSearch(dto, term))
                .collect(Collectors.toCollection(ArrayList::new));
        }

        customers.sort(Comparator.comparing(
            (SellerCustomerDTO dto) -> dto.lastOrderAt == null ? Instant.EPOCH : dto.lastOrderAt)
            .reversed());

        CustomerPage pageDto = paginate(customers, page, size);
        return ResponseEntity.ok(ApiResponse.success(pageDto));
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<SellerCustomerDetailDTO>> getCustomerDetail(
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @PathVariable("customerId") String customerId
    ) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        if (!StringUtils.hasText(customerId)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Customer ID is required"));
        }

        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        List<Object[]> stats = orderRepository.findCustomerStats(seller.getId());
        SellerCustomerDTO summary = stats.stream()
            .map(this::mapToCustomerDTO)
            .filter(dto -> dto != null && customerId.equals(dto.customerId))
            .findFirst()
            .orElse(null);

        if (summary == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Customer not found"));
        }

        SellerCustomerDetailDTO detail = new SellerCustomerDetailDTO();
        detail.customerId = summary.customerId;
        detail.fullName = summary.fullName;
        detail.email = summary.email;
        detail.phone = summary.phone;
        detail.avatarUrl = summary.avatarUrl;
        detail.totalOrders = summary.totalOrders;
        detail.totalSpent = summary.totalSpent;
        detail.lastOrderAt = summary.lastOrderAt;

        Pageable recentOrdersPageable = PageRequest.of(0, 10);
        List<Order> recentOrders = orderRepository
            .findBySellerIdAndCustomerIdOrderByCreatedAtDesc(seller.getId(), customerId, recentOrdersPageable)
            .getContent();

        detail.recentOrders = recentOrders.stream()
            .map(order -> {
                RecentOrderDTO dto = new RecentOrderDTO();
                dto.orderId = order.getId();
                dto.orderNumber = order.getOrderNumber();
                dto.finalTotal = order.getFinalTotal() != null ? order.getFinalTotal().doubleValue() : 0d;
                dto.status = order.getStatus() != null ? order.getStatus().name() : null;
                dto.createdAt = toInstant(order.getCreatedAt());
                return dto;
            })
            .collect(Collectors.toList());

        Order firstOrder = orderRepository
            .findFirstBySellerIdAndCustomerIdOrderByCreatedAtAsc(seller.getId(), customerId);
        if (firstOrder != null) {
            detail.firstOrderAt = toInstant(firstOrder.getCreatedAt());
        }

        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    private SellerCustomerDTO mapToCustomerDTO(Object[] row) {
        if (row == null || row.length < 4) {
            return null;
        }
        User customer = (User) row[0];
        if (customer == null) {
            return null;
        }
        Long totalOrders = (Long) row[1];
        BigDecimal totalSpent = (BigDecimal) row[2];
        SellerCustomerDTO dto = new SellerCustomerDTO();
        dto.customerId = customer.getId();
        dto.fullName = customer.getFullName();
        dto.email = customer.getEmail();
        dto.phone = customer.getPhone();
        dto.avatarUrl = customer.getAvatarUrl();
        dto.totalOrders = totalOrders != null ? totalOrders : 0L;
        dto.totalSpent = totalSpent != null ? totalSpent.doubleValue() : 0d;
        if (row[3] instanceof LocalDateTime localDateTime) {
            dto.lastOrderAt = toInstant(localDateTime);
        } else {
            dto.lastOrderAt = null;
        }
        return dto;
    }

    private boolean matchesSearch(SellerCustomerDTO dto, String term) {
        return (dto.fullName != null && dto.fullName.toLowerCase(Locale.ROOT).contains(term))
            || (dto.email != null && dto.email.toLowerCase(Locale.ROOT).contains(term))
            || (dto.phone != null && dto.phone.toLowerCase(Locale.ROOT).contains(term));
    }

    private CustomerPage paginate(List<SellerCustomerDTO> data, int page, int size) {
        if (size <= 0) {
            size = 20;
        }
        if (page < 0) {
            page = 0;
        }
        int totalElements = data.size();
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<SellerCustomerDTO> content = fromIndex >= toIndex
            ? Collections.emptyList()
            : data.subList(fromIndex, toIndex);

        CustomerPage pageDto = new CustomerPage();
        pageDto.content = new ArrayList<>(content);
        pageDto.totalElements = totalElements;
        pageDto.size = size;
        pageDto.number = page;
        pageDto.totalPages = (int) Math.ceil(totalElements / (double) size);
        return pageDto;
    }

    private Instant toInstant(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    public static class SellerCustomerDTO {
        public String customerId;
        public String fullName;
        public String email;
        public String phone;
        public String avatarUrl;
        public long totalOrders;
        public double totalSpent;
        public Instant lastOrderAt;
    }

    public static class CustomerPage {
        public List<SellerCustomerDTO> content = new ArrayList<>();
        public long totalElements;
        public int totalPages;
        public int size;
        public int number;
    }

    public static class SellerCustomerDetailDTO {
        public String customerId;
        public String fullName;
        public String email;
        public String phone;
        public String avatarUrl;
        public Instant firstOrderAt;
        public Instant lastOrderAt;
        public long totalOrders;
        public double totalSpent;
        public List<RecentOrderDTO> recentOrders = new ArrayList<>();
    }

    public static class RecentOrderDTO {
        public String orderId;
        public String orderNumber;
        public double finalTotal;
        public String status;
        public Instant createdAt;
    }
}

