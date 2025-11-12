package com.shopcuathuy.repository;

import com.shopcuathuy.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CartItemRepository extends JpaRepository<CartItem, String> {
    List<CartItem> findByUserId(String userId);
    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
    Optional<CartItem> findByUserIdAndProductIdAndVariantId(String userId, String productId, String variantId);
    void deleteByUserId(String userId);
}

