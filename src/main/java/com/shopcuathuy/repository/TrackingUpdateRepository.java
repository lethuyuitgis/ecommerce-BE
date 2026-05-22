package com.shopcuathuy.repository;

import com.shopcuathuy.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, String> {
    List<TrackingUpdate> findByShipmentIdOrderByTimestampDesc(String shipmentId);
}
