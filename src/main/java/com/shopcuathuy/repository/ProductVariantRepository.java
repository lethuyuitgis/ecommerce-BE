package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    void deleteByProductId(String productId);
}







