package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    List<ProductImage> findByProductId(String productId);
    List<ProductImage> findByProductIdAndIsPrimaryTrue(String productId);
    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(String productId);
    void deleteByProductId(String productId);
}






