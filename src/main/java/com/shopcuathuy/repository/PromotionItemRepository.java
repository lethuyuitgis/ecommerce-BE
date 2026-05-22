package com.shopcuathuy.repository;

import com.shopcuathuy.entity.PromotionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionItemRepository extends JpaRepository<PromotionItem, String> {
    
    @Query("SELECT pi FROM PromotionItem pi " +
           "JOIN pi.promotion p " +
           "WHERE p.status = 'ACTIVE' " +
           "AND p.startDate <= CURRENT_TIMESTAMP " +
           "AND p.endDate >= CURRENT_TIMESTAMP " +
           "AND (pi.product.id = :productId OR (pi.variant.id IS NOT NULL AND pi.variant.id = :variantId))")
    List<PromotionItem> findActivePromotionsForProduct(String productId, String variantId);
}
