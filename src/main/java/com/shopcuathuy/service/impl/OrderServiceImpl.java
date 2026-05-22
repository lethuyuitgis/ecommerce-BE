package com.shopcuathuy.service.impl;

import com.shopcuathuy.dto.request.CreateOrderRequestDTO;
import com.shopcuathuy.dto.request.UpdateOrderStatusRequestDTO;
import com.shopcuathuy.dto.response.OrderItemResponseDTO;
import com.shopcuathuy.dto.response.OrderPageResponseDTO;
import com.shopcuathuy.dto.response.OrderResponseDTO;
import com.shopcuathuy.dto.response.PurchaseStatusResponseDTO;
import com.shopcuathuy.entity.*;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.exception.ForbiddenException;
import com.shopcuathuy.repository.*;
import com.shopcuathuy.service.OrderService;
import com.shopcuathuy.service.OrderDispatchService;
import com.shopcuathuy.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final VoucherRepository voucherRepository;
    private final OrderDispatchService orderDispatchService;
    private final UserAddressRepository userAddressRepository;
    private final NotificationRepository notificationRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderTimelineRepository orderTimelineRepository;
    private final PromotionItemRepository promotionItemRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final NotificationService notificationService;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           SellerRepository sellerRepository,
                           VoucherRepository voucherRepository,
                           OrderDispatchService orderDispatchService,
                           UserAddressRepository userAddressRepository,
                           NotificationRepository notificationRepository,
                           CartItemRepository cartItemRepository,
                           OrderTimelineRepository orderTimelineRepository,
                           PromotionItemRepository promotionItemRepository,
                           VoucherUsageRepository voucherUsageRepository,
                           NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.voucherRepository = voucherRepository;
        this.orderDispatchService = orderDispatchService;
        this.userAddressRepository = userAddressRepository;
        this.notificationRepository = notificationRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderTimelineRepository = orderTimelineRepository;
        this.promotionItemRepository = promotionItemRepository;
        this.voucherUsageRepository = voucherUsageRepository;
        this.notificationService = notificationService;
    }

    @Override
    public OrderPageResponseDTO getOrders(String userId, String userRole, String status, Pageable pageable) {
        Page<Order> orderPage;
        
        if ("SELLER".equalsIgnoreCase(userRole)) {
            Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            
            if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
                try {
                    Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    orderPage = orderRepository.findBySellerIdAndStatus(seller.getId(), orderStatus, pageable);
                } catch (IllegalArgumentException e) {
                    orderPage = orderRepository.findBySellerId(seller.getId(), pageable);
                }
            } else {
                orderPage = orderRepository.findBySellerId(seller.getId(), pageable);
            }
        } else {
            if (status != null && !status.isEmpty()) {
                try {
                    Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    orderPage = orderRepository.findByCustomerIdAndStatus(userId, orderStatus, pageable);
                } catch (IllegalArgumentException e) {
                    orderPage = orderRepository.findByCustomerId(userId, pageable);
                }
            } else {
                orderPage = orderRepository.findByCustomerId(userId, pageable);
            }
        }

        List<OrderResponseDTO> orderDTOs = orderPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        OrderPageResponseDTO result = new OrderPageResponseDTO();
        result.content = orderDTOs;
        result.totalElements = orderPage.getTotalElements();
        result.totalPages = orderPage.getTotalPages();
        result.size = orderPage.getSize();
        result.number = orderPage.getNumber();

        return result;
    }

    @Override
    public OrderResponseDTO getOrderById(String id, String userId) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (userId != null && !order.getCustomer().getId().equals(userId)) {
            throw new com.shopcuathuy.exception.ForbiddenException("Access denied");
        }

        return convertToDTO(order);
    }

    @Override
    public PurchaseStatusResponseDTO checkPurchase(String productId, String userId) {
        PurchaseStatusResponseDTO status = new PurchaseStatusResponseDTO();
        status.hasPurchased = false;
        status.orderItemId = null;

        if (userId == null || userId.isEmpty()) {
            return status;
        }

        Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 100);
        Page<Order> orders = orderRepository.findByCustomerId(userId, pageable);
        
        for (Order order : orders.getContent()) {
            if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProduct() != null && productId.equals(item.getProduct().getId())) {
                        status.hasPurchased = true;
                        status.orderItemId = item.getId();
                        status.orderId = order.getId();
                        status.orderNumber = order.getOrderNumber();
                        return status;
                    }
                }
            }
        }

        return status;
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(String userId, CreateOrderRequestDTO request) {
        User customer = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use a more robust order number generation
        String orderNumber = "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setShippingStatus(Order.ShippingStatus.PENDING);
        order.setPaymentMethod(request.paymentMethod);
        order.setNotes(request.notes);

        // Snapshot recipient info
        if (request.shippingAddressId != null && !request.shippingAddressId.isEmpty()) {
            UserAddress address = userAddressRepository.findById(request.shippingAddressId).orElse(null);
            if (address != null) {
                order.setRecipientName(address.getFullName());
                order.setRecipientPhone(address.getPhone());
                // UserAddress does not have email, using customer email as fallback or null
                order.setRecipientEmail(customer.getEmail());
                order.setRecipientAddress(address.getStreet());
                order.setRecipientProvince(address.getProvince());
                order.setRecipientDistrict(address.getDistrict());
                order.setRecipientWard(address.getWard());
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        Seller seller = null;
        
        for (com.shopcuathuy.dto.request.CreateOrderItemRequestDTO itemReq : request.items) {
            Product product = productRepository.findByIdWithLock(itemReq.productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId));
            
            // Fix Multi-seller Bug: Throw error if multiple sellers are present in one order
            if (seller == null) {
                seller = product.getSeller();
            } else if (!seller.getId().equals(product.getSeller().getId())) {
                throw new IllegalArgumentException("Đơn hàng không thể chứa sản phẩm từ nhiều người bán khác nhau. Vui lòng tách đơn hàng theo từng shop.");
            }

            ProductVariant variant = null;
            if (itemReq.variantId != null && !itemReq.variantId.isEmpty()) {
                variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(itemReq.variantId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found: " + itemReq.variantId));
                
                if (variant.getVariantQuantity() < itemReq.quantity) {
                    throw new IllegalArgumentException("Biến thể '" + variant.getVariantName() + "' đã hết hàng hoặc không đủ số lượng.");
                }
                variant.setVariantQuantity(variant.getVariantQuantity() - itemReq.quantity);
                if (product.getQuantity() >= itemReq.quantity) {
                    product.setQuantity(product.getQuantity() - itemReq.quantity);
                }
            } else {
                if (product.getQuantity() < itemReq.quantity) {
                    throw new IllegalArgumentException("Sản phẩm '" + product.getName() + "' đã hết hàng hoặc không đủ số lượng.");
                }
                product.setQuantity(product.getQuantity() - itemReq.quantity);
            }
            
            productRepository.save(product);
            
            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID().toString());
            item.setOrder(order);
            item.setProduct(product);
            item.setVariant(variant);
            item.setQuantity(itemReq.quantity);
            
            BigDecimal unitPrice = (variant != null && variant.getVariantPrice() != null) 
                ? variant.getVariantPrice() 
                : product.getPrice();
            
            // 1. Priority: Flash Sale Price
            LocalDateTime now = LocalDateTime.now();
            if (Boolean.TRUE.equals(product.getFlashSaleEnabled()) && 
                product.getFlashSalePrice() != null &&
                product.getFlashSaleStart() != null && product.getFlashSaleEnd() != null &&
                now.isAfter(product.getFlashSaleStart()) && now.isBefore(product.getFlashSaleEnd()) &&
                (product.getFlashSaleStock() == null || product.getFlashSaleStock() >= itemReq.quantity)) {
                
                unitPrice = product.getFlashSalePrice();
                
                // Decrement Flash Sale Stock
                if (product.getFlashSaleStock() != null) {
                    product.setFlashSaleStock(product.getFlashSaleStock() - itemReq.quantity);
                }
                product.setFlashSaleSold(product.getFlashSaleSold() + itemReq.quantity);
            } 
            // 2. Fallback: Standard Promotions
            else {
                List<com.shopcuathuy.entity.PromotionItem> activePromotions = promotionItemRepository.findActivePromotionsForProduct(itemReq.productId, itemReq.variantId);
                if (!activePromotions.isEmpty()) {
                    com.shopcuathuy.entity.Promotion bestPromo = activePromotions.get(0).getPromotion();
                    if (bestPromo.getPromotionType() == com.shopcuathuy.entity.Promotion.PromotionType.PERCENTAGE) {
                        unitPrice = unitPrice.multiply(BigDecimal.ONE.subtract(bestPromo.getDiscountValue().divide(BigDecimal.valueOf(100))));
                    } else if (bestPromo.getPromotionType() == com.shopcuathuy.entity.Promotion.PromotionType.FIXED_AMOUNT) {
                        unitPrice = unitPrice.subtract(bestPromo.getDiscountValue());
                    }
                    if (unitPrice.compareTo(BigDecimal.ZERO) < 0) unitPrice = BigDecimal.ZERO;
                }
            }
                
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(itemReq.quantity)));
            
            // Cart cleanup
            cartItemRepository.findByUserIdAndProductIdAndVariantId(userId, itemReq.productId, itemReq.variantId)
                .ifPresent(cartItemRepository::delete);
            
            order.getOrderItems().add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        if (seller == null) {
            throw new ResourceNotFoundException("Không có sản phẩm hợp lệ trong đơn hàng.");
        }
        order.setSeller(seller);

        // 2. Apply voucher: compute discount first; persist VoucherUsage AFTER order is saved
        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher appliedVoucher = null;
        if (request.voucherCode != null && !request.voucherCode.isEmpty()) {
            Voucher voucher = voucherRepository.findByCodeAndStatus(request.voucherCode, Voucher.VoucherStatus.ACTIVE).orElse(null);
            if (voucher != null && isVoucherValid(voucher, subtotal)) {
                if (voucher.getDiscountType() == Voucher.DiscountType.PERCENTAGE) {
                    discountAmount = subtotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                    if (voucher.getMaxDiscount() != null && discountAmount.compareTo(voucher.getMaxDiscount()) > 0) {
                        discountAmount = voucher.getMaxDiscount();
                    }
                } else if (voucher.getDiscountType() == Voucher.DiscountType.FIXED_AMOUNT) {
                    discountAmount = voucher.getDiscountValue();
                }
                if (discountAmount.compareTo(subtotal) > 0) {
                    discountAmount = subtotal;
                }
                appliedVoucher = voucher;
            }
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(discountAmount);
        // Shipping Fee
        BigDecimal shippingFee = request.shippingFee != null
            ? request.shippingFee
            : BigDecimal.valueOf(30000); // Fallback
        order.setShippingFee(shippingFee);
        order.setTax(subtotal.subtract(discountAmount).multiply(BigDecimal.valueOf(0.1)));
        order.setFinalTotal(subtotal.subtract(discountAmount)
            .add(order.getShippingFee())
            .add(order.getTax()));

        order = orderRepository.save(order);

        // Persist voucher usage AFTER order has an id, so FK to orders is satisfied
        if (appliedVoucher != null) {
            appliedVoucher.setTotalUses(appliedVoucher.getTotalUses() + 1);
            voucherRepository.save(appliedVoucher);

            com.shopcuathuy.entity.VoucherUsage usage = new com.shopcuathuy.entity.VoucherUsage();
            usage.setId(UUID.randomUUID().toString());
            usage.setVoucher(appliedVoucher);
            usage.setCustomer(customer);
            usage.setOrder(order);
            usage.setDiscountAmount(discountAmount);
            usage.setUsedAt(java.time.LocalDateTime.now());
            voucherUsageRepository.save(usage);
        }

        // Add to timeline
        addTimelineEntry(order, "CONFIRMED", "Đơn hàng được đặt thành công.", "customer");
        
        // Send notifications
        notificationService.createAndDispatch(customer, Notification.NotificationType.ORDER_STATUS, "Đặt hàng thành công", 
            "Đơn hàng #" + order.getOrderNumber() + " của bạn đã được tiếp nhận.", "/orders/" + order.getId(), order.getId(), null);

        if (seller != null && seller.getUser() != null) {
            notificationService.createAndDispatch(seller.getUser(), Notification.NotificationType.ORDER_NEW, "Đơn hàng mới", 
                "Bạn có đơn hàng mới #" + order.getOrderNumber() + " từ " + customer.getFullName(), "/seller/orders/" + order.getId(), order.getId(), null);
        }

        try {
            orderDispatchService.dispatchOrder(order.getId());
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class)
                .error("Failed to dispatch order {}: {}", order.getId(), e.getMessage());
        }
        
        return convertToDTO(order);
    }

    private boolean isVoucherValid(Voucher voucher, BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate().isAfter(now) || voucher.getEndDate().isBefore(now)) return false;
        if (voucher.getMinPurchaseAmount() != null && subtotal.compareTo(voucher.getMinPurchaseAmount()) < 0) return false;
        if (voucher.getTotalUsesLimit() != null && voucher.getTotalUses() >= voucher.getTotalUsesLimit()) return false;
        return true;
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(String id, String userId) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (userId != null && !order.getCustomer().getId().equals(userId)) {
            throw new com.shopcuathuy.exception.ForbiddenException("Access denied");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING && 
            order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Đơn hàng không thể hủy ở trạng thái hiện tại.");
        }

        // Return inventory
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                if (item.getVariant() != null) {
                    ProductVariant variant = item.getVariant();
                    variant.setVariantQuantity(variant.getVariantQuantity() + item.getQuantity());
                }
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);

        // Add to timeline
        addTimelineEntry(order, "CANCELLED", "Đơn hàng đã bị hủy.", userId != null && userId.equals(order.getCustomer().getId()) ? "customer" : "seller");

        // Notification for customer
        notificationService.createAndDispatch(order.getCustomer(), Notification.NotificationType.ORDER_STATUS, "Đơn hàng đã hủy", 
            "Đơn hàng #" + order.getOrderNumber() + " của bạn đã bị hủy.", "/orders/" + order.getId(), order.getId(), null);

        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(String id, UpdateOrderStatusRequestDTO request, String userId) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            
        Order.OrderStatus oldStatus = order.getStatus();
        Order.PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        Order.ShippingStatus oldShippingStatus = order.getShippingStatus();
            
        // Security check
        Seller seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new com.shopcuathuy.exception.ForbiddenException("User is not a seller"));
            
        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new com.shopcuathuy.exception.ForbiddenException("You do not have permission to update this order status");
        }
        
        if (request.status != null) {
            try {
                order.setStatus(Order.OrderStatus.valueOf(request.status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status: " + request.status);
            }
        }
        if (request.paymentStatus != null) {
            try {
                order.setPaymentStatus(Order.PaymentStatus.valueOf(request.paymentStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
            }
        }
        if (request.shippingStatus != null) {
            try {
                order.setShippingStatus(Order.ShippingStatus.valueOf(request.shippingStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
            }
        }
        
        order = orderRepository.save(order);

        // Add to timeline and send notifications for changes
        if (request.status != null && !oldStatus.equals(order.getStatus())) {
            addTimelineEntry(order, order.getStatus().name(), "Cập nhật trạng thái đơn hàng: " + order.getStatus().getDisplayName(), "seller");
            notificationService.createAndDispatch(order.getCustomer(), Notification.NotificationType.ORDER_STATUS, "Cập nhật trạng thái đơn hàng", 
                "Trạng thái đơn hàng #" + order.getOrderNumber() + " của bạn đã được cập nhật thành: " + order.getStatus().getDisplayName(), "/orders/" + order.getId(), order.getId(), null);
        }
        if (request.paymentStatus != null && !oldPaymentStatus.equals(order.getPaymentStatus())) {
            addTimelineEntry(order, order.getPaymentStatus().name(), "Cập nhật trạng thái thanh toán: " + order.getPaymentStatus().getDisplayName(), "seller");
            sendNotification(order.getCustomer(), "Cập nhật trạng thái thanh toán", 
                "Trạng thái thanh toán đơn hàng #" + order.getOrderNumber() + " của bạn đã được cập nhật thành: " + order.getPaymentStatus().getDisplayName(),
                Notification.NotificationType.ORDER_STATUS, order.getId(), "/orders/" + order.getId());
        }
        if (request.shippingStatus != null && !oldShippingStatus.equals(order.getShippingStatus())) {
            addTimelineEntry(order, order.getShippingStatus().name(), "Cập nhật trạng thái vận chuyển: " + order.getShippingStatus().getDisplayName(), "seller");
            sendNotification(order.getCustomer(), "Cập nhật trạng thái vận chuyển", 
                "Trạng thái vận chuyển đơn hàng #" + order.getOrderNumber() + " của bạn đã được cập nhật thành: " + order.getShippingStatus().getDisplayName(),
                Notification.NotificationType.ORDER_STATUS, order.getId(), "/orders/" + order.getId());
        }

        // Send notification to seller (if needed, e.g., for payment confirmation from customer)
        // This example focuses on customer notifications for status updates initiated by seller.
        
        return convertToDTO(order);
    }

    public OrderResponseDTO convertToDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.id = order.getId();
        dto.orderNumber = order.getOrderNumber();
        dto.customerId = order.getCustomer() != null ? order.getCustomer().getId() : null;
        dto.customerName = order.getCustomer() != null ? order.getCustomer().getFullName() : null;
        dto.sellerId = order.getSeller() != null ? order.getSeller().getId() : null;
        dto.sellerName = order.getSeller() != null ? order.getSeller().getShopName() : null;
        dto.status = order.getStatus() != null ? order.getStatus().name() : null;
        dto.paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null;
        dto.shippingStatus = order.getShippingStatus() != null ? order.getShippingStatus().name() : null;
        dto.totalPrice = order.getTotalPrice() != null ? order.getTotalPrice().doubleValue() : null;
        dto.subtotal = order.getSubtotal() != null ? order.getSubtotal().doubleValue() : null;
        dto.discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : null;
        dto.shippingFee = order.getShippingFee() != null ? order.getShippingFee().doubleValue() : null;
        dto.tax = order.getTax() != null ? order.getTax().doubleValue() : null;
        dto.finalTotal = order.getFinalTotal() != null ? order.getFinalTotal().doubleValue() : null;
        dto.paymentMethod = order.getPaymentMethod();
        dto.notes = order.getNotes();
        dto.customerNotes = order.getCustomerNotes();
        dto.recipientName = order.getRecipientName();
        dto.recipientPhone = order.getRecipientPhone();
        dto.recipientEmail = order.getRecipientEmail();
        dto.recipientAddress = order.getRecipientAddress();
        dto.recipientProvince = order.getRecipientProvince();
        dto.recipientDistrict = order.getRecipientDistrict();
        dto.recipientWard = order.getRecipientWard();
        dto.createdAt = order.getCreatedAt() != null ?
            order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant() : null;
        
        dto.items = order.getOrderItems() != null ?
            order.getOrderItems().stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList()) :
            null;
        
        return dto;
    }

    private OrderItemResponseDTO convertItemToDTO(OrderItem item) {
        OrderItemResponseDTO dto = new OrderItemResponseDTO();
        dto.id = item.getId();
        
        try {
            dto.productId = item.getProduct() != null ? item.getProduct().getId() : null;
            dto.productName = item.getProduct() != null ? item.getProduct().getName() : null;
            
            if (item.getVariant() != null) {
                dto.variantId = item.getVariant().getId();
                dto.variantName = item.getVariant().getVariantName();
                dto.variantImage = item.getVariant().getVariantImage();
            }
            
            dto.productImage = (dto.variantImage != null && !dto.variantImage.isEmpty())
                ? dto.variantImage
                : (item.getProduct() != null && item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()
                    ? item.getProduct().getImages().get(0).getImageUrl()
                    : null);
        } catch (Exception e) {
            // Handle deleted products/variants
            dto.productName = "[Sản phẩm đã bị xóa]";
        }
        
        dto.quantity = item.getQuantity();
        dto.unitPrice = item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null;
        dto.productPrice = item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null;
        dto.totalPrice = item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null;
        
        return dto;
    }

    private void sendNotification(User recipient, String title, String content, 
                                  Notification.NotificationType type, String relatedId, String linkUrl) {
        try {
            Notification notification = new Notification();
            notification.setId(UUID.randomUUID().toString());
            notification.setRecipient(recipient);
            notification.setTitle(title);
            notification.setMessage(content);
            notification.setType(type);
            notification.setRelatedId(relatedId);
            notification.setLinkUrl(linkUrl);
            notification.setIsRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        } catch (Exception e) {
            // Log but don't break transaction
            log.error("Failed to send notification: {}", e.getMessage());
        }
    }

    private void addTimelineEntry(Order order, String status, String note, String createdBy) {
        com.shopcuathuy.entity.OrderTimeline timeline = new com.shopcuathuy.entity.OrderTimeline();
        timeline.setId(UUID.randomUUID().toString());
        timeline.setOrder(order);
        timeline.setStatus(status);
        timeline.setNote(note);
        timeline.setCreatedBy(createdBy);
        orderTimelineRepository.save(timeline);
    }
}
