package com.shopcuathuy.repository;

import com.shopcuathuy.entity.OrderTimeline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderTimelineRepository extends JpaRepository<OrderTimeline, String> {
    List<OrderTimeline> findByOrderIdOrderByCreatedAtDesc(String orderId);
}
