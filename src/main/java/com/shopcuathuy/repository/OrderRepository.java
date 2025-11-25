package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Order;
import com.shopcuathuy.entity.Seller;
import com.shopcuathuy.entity.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    Order findFirstBySellerIdAndCustomerIdOrderByCreatedAtAsc(String sellerId, String customerId);
    Order findFirstBySellerIdAndCustomerIdOrderByCreatedAtDesc(String sellerId, String customerId);
    long countBySellerIdAndCustomerId(String sellerId, String customerId);

    @Query("SELECT COALESCE(SUM(o.finalTotal),0) FROM Order o WHERE o.seller.id = :sellerId AND o.customer.id = :customerId")
    BigDecimal sumFinalTotalBySellerIdAndCustomerId(String sellerId, String customerId);

    List<Order> findBySellerIdAndCreatedAtBetween(String sellerId, LocalDateTime start, LocalDateTime end);
    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Optimized query with JOIN FETCH to avoid N+1 problem
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.customer WHERE o.seller.id = :sellerId AND o.createdAt BETWEEN :start AND :end ORDER BY o.createdAt DESC")
    List<Order> findBySellerIdAndCreatedAtBetweenWithCustomer(String sellerId, LocalDateTime start, LocalDateTime end);

    // Aggregation query for revenue statistics
    @Query("SELECT COALESCE(SUM(o.finalTotal), 0) FROM Order o WHERE o.seller.id = :sellerId AND o.createdAt BETWEEN :start AND :end AND o.status = :status")
    BigDecimal sumRevenueBySellerIdAndDateRangeAndStatus(String sellerId, LocalDateTime start, LocalDateTime end, Order.OrderStatus status);

    // Count query for completed orders
    @Query("SELECT COUNT(o) FROM Order o WHERE o.seller.id = :sellerId AND o.createdAt BETWEEN :start AND :end AND o.status = :status")
    long countBySellerIdAndDateRangeAndStatus(String sellerId, LocalDateTime start, LocalDateTime end, Order.OrderStatus status);

    // Count all orders in date range
    @Query("SELECT COUNT(o) FROM Order o WHERE o.seller.id = :sellerId AND o.createdAt BETWEEN :start AND :end")
    long countBySellerIdAndDateRange(String sellerId, LocalDateTime start, LocalDateTime end);

    // Status breakdown aggregation
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.seller.id = :sellerId AND o.createdAt BETWEEN :start AND :end GROUP BY o.status")
    List<Object[]> countBySellerIdAndDateRangeGroupByStatus(String sellerId, LocalDateTime start, LocalDateTime end);

    // Check if customer owns the order
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.id = :orderId AND o.customer.id = :customerId")
    boolean existsByIdAndCustomerId(String orderId, String customerId);

}

