package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Page<Promotion> findBySellerId(String sellerId, Pageable pageable);
    
    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' AND p.startDate <= :now AND p.endDate >= :now ORDER BY p.endDate ASC")
    Page<Promotion> findActivePromotions(@Param("now") java.time.LocalDateTime now, Pageable pageable);
}


