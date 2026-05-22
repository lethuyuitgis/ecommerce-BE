package com.shopcuathuy.service;

import com.shopcuathuy.entity.Shipment;
import com.shopcuathuy.entity.ShippingHub;
import com.shopcuathuy.entity.TrackingUpdate;
import com.shopcuathuy.repository.ShipmentRepository;
import com.shopcuathuy.repository.ShippingHubRepository;
import com.shopcuathuy.repository.TrackingUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShipmentRepository shipmentRepository;
    private final TrackingUpdateRepository trackingUpdateRepository;
    private final ShippingHubRepository shippingHubRepository;

    @Transactional
    public void addTrackingUpdate(String shipmentId, String status, String location, String description) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));

        TrackingUpdate update = new TrackingUpdate();
        update.setId(UUID.randomUUID().toString());
        update.setShipment(shipment);
        update.setStatus(status);
        update.setLocation(location);
        update.setDescription(description);
        update.setTimestamp(LocalDateTime.now());
        trackingUpdateRepository.save(update);

        // Update shipment's current status if provided
        try {
            Shipment.ShipmentStatus newStatus = Shipment.ShipmentStatus.valueOf(status.toUpperCase());
            shipment.setStatus(newStatus);
            shipmentRepository.save(shipment);
        } catch (Exception ignored) {
            // Description might not be a valid status
        }
    }

    @Transactional
    public void assignToHub(String shipmentId, String hubId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        ShippingHub hub = shippingHubRepository.findById(hubId)
                .orElseThrow(() -> new IllegalArgumentException("Hub not found"));

        shipment.setCurrentHub(hub);
        shipmentRepository.save(shipment);

        addTrackingUpdate(shipmentId, "ARRIVED_HUB", hub.getName(), 
                "Đơn hàng đã đến kho: " + hub.getAddress());
    }

    @Transactional
    public void updateCodStatus(String shipmentId, Shipment.CodStatus status) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found"));
        shipment.setCodStatus(status);
        shipmentRepository.save(shipment);
    }

    public List<ShippingHub> getAllHubs() {
        return shippingHubRepository.findAll();
    }

    public List<TrackingUpdate> getTrackingHistory(String shipmentId) {
        return trackingUpdateRepository.findByShipmentIdOrderByTimestampDesc(shipmentId);
    }
}
