package com.shopcuathuy.service;

import com.shopcuathuy.dto.CreateOrderRequest;
import com.shopcuathuy.dto.OrderDTO;
import com.shopcuathuy.entity.*;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.*;
import com.shopcuathuy.service.NotificationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;
    private final UserAddressRepository userAddressRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final NotificationService notificationService;
    
    public OrderDTO createOrder(String userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserAddress shippingAddress = userAddressRepository.findById(request.getShippingAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping address not found"));
        
        if (!shippingAddress.getUser().getId().equals(userId)) {
            throw new BadRequestException("Invalid shipping address");
        }
        
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomer(user);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setShippingStatus(Order.ShippingStatus.PENDING);
        order.setNotes(request.getNotes());
        order.setPaymentMethod(request.getPaymentMethod());
        
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        String sellerId = null;
        
        // Process order items
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));
            
            if (product.getStatus() != Product.ProductStatus.ACTIVE) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }
            
            if (sellerId == null) {
                sellerId = product.getSeller().getId();
                order.setSeller(product.getSeller());
            } else if (!sellerId.equals(product.getSeller().getId())) {
                throw new BadRequestException("All products must be from the same seller");
            }
            
            ProductVariant variant = null;
            if (itemRequest.getVariantId() != null) {
                variant = productVariantRepository.findById(itemRequest.getVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
            }
            
            // Check availability
            int availableQuantity = variant != null ? variant.getVariantQuantity() : product.getQuantity();
            if (availableQuantity < itemRequest.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName());
            }
            
            BigDecimal unitPrice = variant != null && variant.getVariantPrice() != null
                    ? variant.getVariantPrice()
                    : product.getPrice();
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setVariant(variant);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
            
            order.getOrderItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getTotalPrice());
            
            // Update inventory
            if (variant != null) {
                variant.setVariantQuantity(variant.getVariantQuantity() - itemRequest.getQuantity());
                productVariantRepository.save(variant);
            } else {
                product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
                productRepository.save(product);
            }
        }
        
        order.setSubtotal(subtotal);
        order.setTotalPrice(subtotal);
        
        // Apply voucher if provided
        if (request.getVoucherCode() != null && !request.getVoucherCode().isEmpty()) {
            Voucher voucher = voucherRepository.findByCode(request.getVoucherCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
            
            // Validate voucher
            LocalDateTime now = LocalDateTime.now();
            if (voucher.getStatus() != Voucher.VoucherStatus.ACTIVE ||
                now.isBefore(voucher.getStartDate()) ||
                now.isAfter(voucher.getEndDate())) {
                throw new BadRequestException("Voucher is not valid");
            }
            
            if (voucher.getTotalUsesLimit() != null && voucher.getTotalUses() >= voucher.getTotalUsesLimit()) {
                throw new BadRequestException("Voucher has reached usage limit");
            }
            
            // Calculate discount
            if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                discountAmount = subtotal.multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
            } else if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
                discountAmount = voucher.getDiscountValue();
            }
            
            if (discountAmount.compareTo(subtotal) > 0) {
                discountAmount = subtotal;
            }
            
            order.setDiscountAmount(discountAmount);
            
            // Record voucher usage
            VoucherUsage voucherUsage = new VoucherUsage();
            voucherUsage.setVoucher(voucher);
            voucherUsage.setCustomer(user);
            voucherUsage.setOrder(order);
            voucherUsage.setDiscountAmount(discountAmount);
            voucherUsage.setUsedAt(now);
            voucherUsageRepository.save(voucherUsage);
            
            voucher.setTotalUses(voucher.getTotalUses() + 1);
            voucherRepository.save(voucher);
        }
        
        BigDecimal shippingFee = BigDecimal.ZERO; // TODO: Calculate shipping fee
        BigDecimal finalTotal = subtotal.subtract(discountAmount).add(shippingFee);
        
        order.setShippingFee(shippingFee);
        order.setFinalTotal(finalTotal);
        
        order = orderRepository.save(order);
        notificationService.notifyOrderCreated(order);
        
        // Clear cart items if order is created from cart
        cartItemRepository.deleteByUserId(userId);
        
        return toDTO(order);
    }
    
    public Page<OrderDTO> getUserOrders(String userId, Pageable pageable) {
        return orderRepository.findByCustomerId(userId, pageable)
                .map(this::toDTO);
    }
    
    public OrderDTO getOrderById(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Check if user is the seller or customer
        boolean isSeller = order.getSeller().getUser().getId().equals(userId);
        boolean isCustomer = order.getCustomer().getId().equals(userId);
        
        if (!isSeller && !isCustomer) {
            throw new BadRequestException("Unauthorized");
        }
        
        return toDTO(order);
    }
    
    public OrderDTO updateOrderStatus(String orderId, String userId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        // Check if user is the seller or customer
        boolean isSeller = order.getSeller().getUser().getId().equals(userId);
        boolean isCustomer = order.getCustomer().getId().equals(userId);
        
        if (!isSeller && !isCustomer) {
            throw new BadRequestException("Unauthorized");
        }
        
        // Map frontend status to backend enum
        Order.OrderStatus newStatus;
        try {
            // Handle frontend status names
            String statusUpper = status.toUpperCase();
            if (statusUpper.equals("PROCESSING")) {
                newStatus = Order.OrderStatus.CONFIRMED;
            } else if (statusUpper.equals("SHIPPING")) {
                newStatus = Order.OrderStatus.SHIPPED;
            } else {
                newStatus = Order.OrderStatus.valueOf(statusUpper);
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status: " + status);
        }
        
        // Validate status transition
        Order.OrderStatus currentStatus = order.getStatus();
        if (currentStatus == Order.OrderStatus.CANCELLED || currentStatus == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot update status of cancelled or delivered order");
        }
        
        order.setStatus(newStatus);
        
        // Update shipping status based on order status
        if (newStatus == Order.OrderStatus.SHIPPED) {
            order.setShippingStatus(Order.ShippingStatus.IN_TRANSIT);
        } else if (newStatus == Order.OrderStatus.DELIVERED) {
            order.setShippingStatus(Order.ShippingStatus.DELIVERED);
        }
        
        order = orderRepository.save(order);
        notificationService.notifyOrderStatusChanged(order, newStatus, userId);
        return toDTO(order);
    }
    
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "SHO" + timestamp;
    }
    
    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomer().getId());
        dto.setCustomerName(order.getCustomer().getFullName());
        dto.setSellerId(order.getSeller().getId());
        dto.setSellerName(order.getSeller().getShopName());
        
        // Map backend status to frontend format
        String status = order.getStatus().name();
        if (status.equals("CONFIRMED")) {
            status = "PROCESSING";
        } else if (status.equals("SHIPPED")) {
            status = "SHIPPING";
        }
        dto.setStatus(status);
        
        dto.setPaymentStatus(order.getPaymentStatus().name());
        dto.setShippingStatus(order.getShippingStatus().name());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setSubtotal(order.getSubtotal());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setShippingFee(order.getShippingFee());
        dto.setTax(order.getTax());
        dto.setFinalTotal(order.getFinalTotal());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setNotes(order.getNotes());
        dto.setCustomerNotes(order.getCustomerNotes());
        dto.setCreatedAt(order.getCreatedAt());
        
        dto.setItems(order.getOrderItems().stream()
                .map(this::toOrderItemDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }
    
    private com.shopcuathuy.dto.OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        com.shopcuathuy.dto.OrderItemDTO dto = new com.shopcuathuy.dto.OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setTotalPrice(orderItem.getTotalPrice());
        
        if (orderItem.getVariant() != null) {
            dto.setVariantId(orderItem.getVariant().getId());
            dto.setVariantName(orderItem.getVariant().getVariantName());
        }
        
        orderItem.getProduct().getImages().stream()
                .filter(img -> img.getIsPrimary())
                .findFirst()
                .ifPresent(img -> dto.setProductImage(img.getImageUrl()));
        
        return dto;
    }
}

