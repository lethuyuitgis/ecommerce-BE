package com.shopcuathuy.service.impl;

import com.shopcuathuy.dto.request.CreateOrderRequestDTO;
import com.shopcuathuy.dto.request.UpdateOrderStatusRequestDTO;
import com.shopcuathuy.dto.response.OrderItemResponseDTO;
import com.shopcuathuy.dto.response.OrderPageResponseDTO;
import com.shopcuathuy.dto.response.OrderResponseDTO;
import com.shopcuathuy.dto.response.PurchaseStatusResponseDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.OrderItem;
import com.shopcuathuy.entity.Product;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ProductRepository;
import com.shopcuathuy.repository.SellerRepository;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private static int orderNumberCounter = 1000;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository,
                           SellerRepository sellerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public OrderPageResponseDTO getOrders(String userId, String userRole, String status, Pageable pageable) {
        Page<Order> orderPage;
        
        // Check if user is a seller
        if ("SELLER".equalsIgnoreCase(userRole)) {
            Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
            
            orderPage = orderRepository.findBySellerId(seller.getId(), pageable);
            
            // Filter by status if provided
            if (status != null && !status.isEmpty()) {
                try {
                    Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                    List<Order> filteredOrders = orderPage.getContent().stream()
                        .filter(o -> o.getStatus() == orderStatus)
                        .collect(Collectors.toList());
                    orderPage = new PageImpl<>(filteredOrders, pageable, filteredOrders.size());
                } catch (IllegalArgumentException e) {
                    // Invalid status, ignore
                }
            }
        } else {
            // User is a customer
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

        // Check authorization
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

        // Check if user has any delivered orders with this product
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

        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setOrderNumber("ORD" + (orderNumberCounter++));
        order.setCustomer(customer);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setShippingStatus(Order.ShippingStatus.PENDING);
        order.setPaymentMethod(request.paymentMethod);
        order.setNotes(request.notes);

        // Calculate totals
        BigDecimal subtotal = BigDecimal.ZERO;
        Product firstProduct = null;
        
        for (com.shopcuathuy.dto.request.CreateOrderItemRequestDTO itemReq : request.items) {
            Product product = productRepository.findById(itemReq.productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId));
            
            if (firstProduct == null) {
                firstProduct = product;
            }
            
            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID().toString());
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.quantity);
            item.setUnitPrice(product.getPrice());
            item.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity)));
            
            order.getOrderItems().add(item);
            subtotal = subtotal.add(item.getTotalPrice());
        }

        // Set seller from first product
        if (firstProduct != null && firstProduct.getSeller() != null) {
            order.setSeller(firstProduct.getSeller());
        } else {
            throw new ResourceNotFoundException("Seller not found for product");
        }

        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO); // Apply voucher if needed
        order.setShippingFee(BigDecimal.valueOf(30000)); // Mock shipping fee
        order.setTax(subtotal.multiply(BigDecimal.valueOf(0.1))); // 10% tax
        order.setFinalTotal(subtotal.subtract(order.getDiscountAmount())
            .add(order.getShippingFee())
            .add(order.getTax()));

        order = orderRepository.save(order);
        return convertToDTO(order);
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
            throw new IllegalArgumentException("Order cannot be cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        return convertToDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(String id, UpdateOrderStatusRequestDTO request) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

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
                // Ignore invalid payment status
            }
        }
        if (request.shippingStatus != null) {
            try {
                order.setShippingStatus(Order.ShippingStatus.valueOf(request.shippingStatus.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Ignore invalid shipping status
            }
        }

        order = orderRepository.save(order);
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
        dto.productId = item.getProduct() != null ? item.getProduct().getId() : null;
        dto.productName = item.getProduct() != null ? item.getProduct().getName() : null;
        dto.variantId = item.getVariant() != null ? item.getVariant().getId() : null;
        dto.variantName = item.getVariant() != null ? item.getVariant().getVariantName() : null;
        dto.quantity = item.getQuantity();
        dto.unitPrice = item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null;
        dto.productPrice = item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : null;
        dto.variantPrice = null; // Not stored separately
        dto.totalPrice = item.getTotalPrice() != null ? item.getTotalPrice().doubleValue() : null;
        dto.productImage = item.getProduct() != null && 
            item.getProduct().getImages() != null && 
            !item.getProduct().getImages().isEmpty() ?
            item.getProduct().getImages().get(0).getImageUrl() : null;
        return dto;
    }
}

