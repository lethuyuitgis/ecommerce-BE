package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Product;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByStatus(Product.ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryId(String categoryId, Pageable pageable);
    Page<Product> findBySellerId(String sellerId, Pageable pageable);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.status = :status AND p.isFeatured = true")
    Page<Product> findFeaturedProducts(@Param("status") Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.slug = :slug AND p.status = 'ACTIVE'")
    Page<Product> findByCategorySlug(@Param("slug") String slug, Pageable pageable);
    
    long countBySellerId(String sellerId);
    
    Optional<Product> findBySku(String sku);
}

