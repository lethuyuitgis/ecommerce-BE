package com.shopcuathuy.repository;

import com.shopcuathuy.entity.OrderItem;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("SELECT oi.product, SUM(oi.quantity), COALESCE(SUM(oi.totalPrice), 0) "
        + "FROM OrderItem oi "
        + "WHERE oi.order.seller.id = :sellerId "
        + "AND oi.order.createdAt BETWEEN :start AND :end "
        + "GROUP BY oi.product "
        + "ORDER BY COALESCE(SUM(oi.totalPrice), 0) DESC")
    List<Object[]> findProductPerformance(@Param("sellerId") String sellerId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(oi.product.category.name, 'Khác'), COALESCE(SUM(oi.totalPrice), 0) "
        + "FROM OrderItem oi "
        + "WHERE oi.order.seller.id = :sellerId "
        + "AND oi.order.createdAt BETWEEN :start AND :end "
        + "GROUP BY COALESCE(oi.product.category.name, 'Khác')")
    List<Object[]> findCategoryBreakdown(@Param("sellerId") String sellerId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE(o.created_at) as order_date, " +
           "COALESCE(SUM(oi.total_price), 0) as revenue, " +
           "COALESCE(SUM(oi.quantity), 0) as quantity " +
           "FROM order_items oi " +
           "INNER JOIN orders o ON oi.order_id = o.id " +
           "WHERE oi.product_id = :productId " +
           "AND o.created_at BETWEEN :start AND :end " +
           "AND o.status = 'DELIVERED' " +
           "GROUP BY DATE(o.created_at) " +
           "ORDER BY DATE(o.created_at) ASC", nativeQuery = true)
    List<Object[]> findSalesByProductIdAndDateRange(@Param("productId") String productId,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    // Check if customer has purchased a product (through any order)
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.customer.id = :customerId")
    boolean existsByProductIdAndCustomerId(@Param("productId") String productId, @Param("customerId") String customerId);
}








