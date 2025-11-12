package com.shopcuathuy.service;

import com.shopcuathuy.dto.SellerCustomerDTO;
import com.shopcuathuy.dto.SellerCustomerDetailDTO;
import com.shopcuathuy.dto.SellerCustomerDetailDTO.OrderSummaryDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SellerCustomerService {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public Page<SellerCustomerDTO> getCustomers(String sellerUserId, String keyword, Pageable pageable) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        List<Object[]> rows = orderRepository.findCustomerStats(seller.getId());
        List<SellerCustomerDTO> all = new ArrayList<>();
        for (Object[] row : rows) {
            User customer = (User) row[0];
            Long totalOrders = ((Number) row[1]).longValue();
            BigDecimal totalSpent = (BigDecimal) row[2];
            LocalDateTime lastOrderAt = (LocalDateTime) row[3];

            SellerCustomerDTO dto = new SellerCustomerDTO();
            dto.setCustomerId(customer.getId());
            dto.setFullName(customer.getFullName());
            dto.setEmail(customer.getEmail());
            dto.setPhone(customer.getPhone());
            dto.setTotalOrders(totalOrders);
            dto.setTotalSpent(totalSpent);
            dto.setLastOrderAt(lastOrderAt);
            all.add(dto);
        }

        if (keyword != null && !keyword.isBlank()) {
            String q = keyword.toLowerCase(Locale.ROOT);
            all = all.stream()
                    .filter(dto -> containsIgnoreCase(dto.getFullName(), q)
                            || containsIgnoreCase(dto.getEmail(), q)
                            || containsIgnoreCase(dto.getPhone(), q))
                    .collect(Collectors.toList());
        }

        all.sort(Comparator.comparing(SellerCustomerDTO::getLastOrderAt, Comparator.nullsLast(Comparator.reverseOrder())));

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), all.size());
        List<SellerCustomerDTO> content = start > end ? List.of() : all.subList(start, end);

        return new PageImpl<>(content, pageable, all.size());
    }

    public SellerCustomerDetailDTO getCustomerDetail(String sellerUserId, String customerId) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Order> orders = orderRepository
                .findBySellerIdAndCustomerIdOrderByCreatedAtDesc(seller.getId(), customerId, Pageable.unpaged())
                .getContent();

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("Customer has no orders with this seller");
        }

        SellerCustomerDetailDTO dto = new SellerCustomerDetailDTO();
        dto.setCustomerId(customer.getId());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAvatarUrl(customer.getAvatarUrl());

        dto.setTotalOrders((long) orders.size());
        dto.setTotalSpent(orders.stream()
                .map(Order::getFinalTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        dto.setLastOrderAt(orders.get(0).getCreatedAt());
        dto.setFirstOrderAt(orders.get(orders.size() - 1).getCreatedAt());

        List<OrderSummaryDTO> summaries = orders.stream()
                .limit(10)
                .map(order -> {
                    OrderSummaryDTO summary = new OrderSummaryDTO();
                    summary.setOrderId(order.getId());
                    summary.setOrderNumber(order.getOrderNumber());
                    summary.setFinalTotal(order.getFinalTotal());
                    summary.setStatus(order.getStatus().name());
                    summary.setCreatedAt(order.getCreatedAt());
                    return summary;
                })
                .collect(Collectors.toList());
        dto.setRecentOrders(summaries);

        return dto;
    }

    private boolean containsIgnoreCase(String source, String needle) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(needle);
    }
}
