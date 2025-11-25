package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Shipment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Optional<Shipment> findByOrderId(String orderId);
    List<Shipment> findByStatus(Shipment.ShipmentStatus status);
}

