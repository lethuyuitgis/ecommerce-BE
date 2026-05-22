package com.shopcuathuy.repository;

import com.shopcuathuy.entity.InventoryHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, String> {
    Page<InventoryHistory> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);
}
