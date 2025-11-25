package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Product;
import java.util.List;
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
    
    // Method name query - Spring Data JPA will handle Boolean mapping
    // Use LEFT JOIN to handle missing categories gracefully
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status AND p.isFeatured = :isFeatured")
    Page<Product> findByStatusAndIsFeatured(@Param("status") Product.ProductStatus status, @Param("isFeatured") Boolean isFeatured, Pageable pageable);
    
    // JPQL query using = true with LEFT JOIN to handle missing categories
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status AND p.isFeatured = true")
    Page<Product> findFeaturedProducts(@Param("status") Product.ProductStatus status, Pageable pageable);
    
    // Alternative JPQL query using parameter with LEFT JOIN
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status AND p.isFeatured = :isFeatured")
    Page<Product> findFeaturedProductsByParam(@Param("status") Product.ProductStatus status, @Param("isFeatured") Boolean isFeatured, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.slug = :slug AND p.status = :status")
    Page<Product> findByCategorySlug(@Param("slug") String slug, @Param("status") Product.ProductStatus status, Pageable pageable);
    
    long countBySellerId(String sellerId);
    long countBySellerIdAndStatus(String sellerId, Product.ProductStatus status);
    
    Optional<Product> findBySku(String sku);

    List<Product> findTop5BySellerIdOrderByTotalSoldDesc(String sellerId);

    List<Product> findTop5BySellerIdOrderByQuantityAsc(String sellerId);

    // Optimized query with JOIN FETCH to avoid N+1 problem
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category WHERE p.seller.id = :sellerId ORDER BY p.createdAt DESC")
    List<Product> findBySellerIdWithCategory(@Param("sellerId") String sellerId, Pageable pageable);
}

