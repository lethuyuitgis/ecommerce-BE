package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Order;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByCustomerId(String customerId, Pageable pageable);
    Page<Order> findBySellerId(String sellerId, Pageable pageable);
    Page<Order> findByOrderNumber(String orderNumber, Pageable pageable);
    Page<Order> findByCustomerIdAndStatus(String customerId, Order.OrderStatus status, Pageable pageable);
    @Query("SELECT o.customer, COUNT(o), COALESCE(SUM(o.finalTotal),0), MAX(o.createdAt) FROM Order o WHERE o.seller.id = :sellerId GROUP BY o.customer")
    List<Object[]> findCustomerStats(String sellerId);

    @Query("SELECT o FROM Order o WHERE o.seller.id = :sellerId AND o.customer.id = :customerId ORDER BY o.createdAt DESC")
    Page<Order> findBySellerIdAndCustomerIdOrderByCreatedAtDesc(String sellerId, String customerId, Pageable pageable);

}

