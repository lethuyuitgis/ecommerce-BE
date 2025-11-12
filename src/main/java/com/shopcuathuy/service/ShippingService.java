package com.shopcuathuy.service;

import com.shopcuathuy.dto.ShipmentDTO;
import com.shopcuathuy.dto.TrackingUpdateDTO;
import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ShippingMethod;
import com.shopcuathuy.entity.TrackingUpdate;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.OrderRepository;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.ShippingMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShippingService {
    
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    
    // simple in-memory settings (could be persisted later)
    private final AtomicReference<String> defaultMethodIdRef = new AtomicReference<>(null);
    private final AtomicReference<Boolean> freeShippingEnabledRef = new AtomicReference<>(false);
    private final AtomicReference<BigDecimal> minOrderValueRef = new AtomicReference<>(null);
    
    public ShipmentDTO createShipment(String orderId, CreateShipmentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));
        
        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setShippingMethod(shippingMethod);
        shipment.setTrackingNumber(generateTrackingNumber());
        shipment.setSenderName(request.getSenderName());
        shipment.setSenderPhone(request.getSenderPhone());
        shipment.setRecipientName(request.getRecipientName());
        shipment.setRecipientPhone(request.getRecipientPhone());
        shipment.setRecipientAddress(request.getRecipientAddress());
        shipment.setRecipientProvince(request.getRecipientProvince());
        shipment.setRecipientDistrict(request.getRecipientDistrict());
        shipment.setRecipientWard(request.getRecipientWard());
        shipment.setWeight(request.getWeight());
        shipment.setShippingFee(request.getShippingFee());
        shipment.setStatus(Shipment.ShipmentStatus.PENDING);
        shipment.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        
        shipment = shipmentRepository.save(shipment);
        
        // Update order shipping status
        order.setShippingStatus(Order.ShippingStatus.PICKED_UP);
        orderRepository.save(order);
        
        return toDTO(shipment);
    }
    
    public ShipmentDTO getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        return toDTO(shipment);
    }
    
    public ShipmentDTO getShipmentByOrderId(String orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        return toDTO(shipment);
    }
    
    public ShipmentDTO updateShipmentStatus(String shipmentId, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        
        shipment.setStatus(Shipment.ShipmentStatus.valueOf(request.getStatus()));
        
        if (request.getLocation() != null) {
            // Add tracking update
            TrackingUpdate trackingUpdate = new TrackingUpdate();
            trackingUpdate.setShipment(shipment);
            trackingUpdate.setStatus(request.getStatus());
            trackingUpdate.setLocation(request.getLocation());
            trackingUpdate.setDescription(request.getDescription());
            trackingUpdate.setTimestamp(LocalDateTime.now());
            shipment.getTrackingUpdates().add(trackingUpdate);
        }
        
        if (Shipment.ShipmentStatus.DELIVERED.name().equals(request.getStatus())) {
            shipment.setActualDeliveryDate(java.time.LocalDate.now());
            // Update order status
            shipment.getOrder().setShippingStatus(Order.ShippingStatus.DELIVERED);
            shipment.getOrder().setStatus(Order.OrderStatus.DELIVERED);
            orderRepository.save(shipment.getOrder());
        }
        
        shipment = shipmentRepository.save(shipment);
        return toDTO(shipment);
    }
    
    public List<TrackingUpdateDTO> getTrackingUpdates(String shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        
        return shipment.getTrackingUpdates().stream()
                .map(this::toTrackingUpdateDTO)
                .collect(Collectors.toList());
    }
    
    public BigDecimal calculateShippingFee(String shippingMethodId, String province, String district, BigDecimal weight) {
        ShippingMethod method = shippingMethodRepository.findById(shippingMethodId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));
        
        // Simplified calculation - in production, integrate with shipping partner APIs
        BigDecimal baseFee = new BigDecimal("25000"); // Base fee
        BigDecimal weightFee = weight.multiply(new BigDecimal("5000")); // 5000 per kg
        
        return baseFee.add(weightFee);
    }
    
    public ShippingMethod updateMethodActive(String id, boolean isActive) {
        ShippingMethod method = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));
        method.setIsActive(isActive);
        return shippingMethodRepository.save(method);
    }
    
    public void setDefaultMethod(String methodId) {
        // Validate exists
        shippingMethodRepository.findById(methodId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));
        defaultMethodIdRef.set(methodId);
    }
    
    public void saveSettings(boolean freeEnabled, BigDecimal minOrder) {
        freeShippingEnabledRef.set(freeEnabled);
        minOrderValueRef.set(minOrder);
    }
    
    private String generateTrackingNumber() {
        return "TRK" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
    
    private ShipmentDTO toDTO(Shipment shipment) {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(shipment.getId());
        dto.setOrderId(shipment.getOrder().getId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setShippingMethodId(shipment.getShippingMethod().getId());
        dto.setShippingMethodName(shipment.getShippingMethod().getName());
        dto.setSenderName(shipment.getSenderName());
        dto.setSenderPhone(shipment.getSenderPhone());
        dto.setRecipientName(shipment.getRecipientName());
        dto.setRecipientPhone(shipment.getRecipientPhone());
        dto.setRecipientAddress(shipment.getRecipientAddress());
        dto.setRecipientProvince(shipment.getRecipientProvince());
        dto.setRecipientDistrict(shipment.getRecipientDistrict());
        dto.setRecipientWard(shipment.getRecipientWard());
        dto.setWeight(shipment.getWeight());
        dto.setShippingFee(shipment.getShippingFee());
        dto.setStatus(shipment.getStatus().name());
        dto.setExpectedDeliveryDate(shipment.getExpectedDeliveryDate());
        dto.setActualDeliveryDate(shipment.getActualDeliveryDate());
        return dto;
    }
    
    private TrackingUpdateDTO toTrackingUpdateDTO(TrackingUpdate update) {
        TrackingUpdateDTO dto = new TrackingUpdateDTO();
        dto.setId(update.getId());
        dto.setStatus(update.getStatus());
        dto.setLocation(update.getLocation());
        dto.setDescription(update.getDescription());
        dto.setTimestamp(update.getTimestamp());
        return dto;
    }
    
    public static class CreateShipmentRequest {
        private String shippingMethodId;
        private String senderName;
        private String senderPhone;
        private String recipientName;
        private String recipientPhone;
        private String recipientAddress;
        private String recipientProvince;
        private String recipientDistrict;
        private String recipientWard;
        private BigDecimal weight;
        private BigDecimal shippingFee;
        private java.time.LocalDate expectedDeliveryDate;
        
        // Getters and setters
        public String getShippingMethodId() { return shippingMethodId; }
        public void setShippingMethodId(String shippingMethodId) { this.shippingMethodId = shippingMethodId; }
        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }
        public String getSenderPhone() { return senderPhone; }
        public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }
        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
        public String getRecipientPhone() { return recipientPhone; }
        public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
        public String getRecipientAddress() { return recipientAddress; }
        public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }
        public String getRecipientProvince() { return recipientProvince; }
        public void setRecipientProvince(String recipientProvince) { this.recipientProvince = recipientProvince; }
        public String getRecipientDistrict() { return recipientDistrict; }
        public void setRecipientDistrict(String recipientDistrict) { this.recipientDistrict = recipientDistrict; }
        public String getRecipientWard() { return recipientWard; }
        public void setRecipientWard(String recipientWard) { this.recipientWard = recipientWard; }
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
        public java.time.LocalDate getExpectedDeliveryDate() { return expectedDeliveryDate; }
        public void setExpectedDeliveryDate(java.time.LocalDate expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    }
    
    public static class UpdateShipmentStatusRequest {
        private String status;
        private String location;
        private String description;
        
        // Getters and setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}




