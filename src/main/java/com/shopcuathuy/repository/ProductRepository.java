package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.math.BigDecimal;
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

    @Query("SELECT p FROM Product p " +
           "WHERE p.seller.id = :sellerId " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<Product> searchSellerProducts(@Param("sellerId") String sellerId,
                                       @Param("keyword") String keyword,
                                       @Param("categoryId") String categoryId,
                                       @Param("status") Product.ProductStatus status,
                                       Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.images " +
           "WHERE p.status = :status " +
           "AND (:keyword IS NULL OR (" +
           "    LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "    OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "    OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "    OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "    OR LOWER(p.seller.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
           ")) " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minRating IS NULL OR p.rating >= :minRating)")
    Page<Product> searchProductsWithFilters(@Param("status") Product.ProductStatus status, 
                                            @Param("keyword") String keyword, 
                                            @Param("categoryId") String categoryId,
                                            @Param("minPrice") BigDecimal minPrice,
                                            @Param("maxPrice") BigDecimal maxPrice,
                                            @Param("minRating") BigDecimal minRating,
                                            Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.seller " +
           "LEFT JOIN FETCH p.images " +
           "WHERE (p.category.slug = :slug OR p.category.parent.slug = :slug) " +
           "AND p.status = :status " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:minRating IS NULL OR p.rating >= :minRating)")
    Page<Product> findByCategorySlugWithFilters(@Param("slug") String slug, 
                                                @Param("status") Product.ProductStatus status, 
                                                @Param("minPrice") BigDecimal minPrice,
                                                @Param("maxPrice") BigDecimal maxPrice,
                                                @Param("minRating") BigDecimal minRating,
                                                Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status AND p.isFeatured = :isFeatured")
    Page<Product> findByStatusAndIsFeatured(@Param("status") Product.ProductStatus status, @Param("isFeatured") Boolean isFeatured, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status AND p.isFeatured = true")
    Page<Product> findFeaturedProducts(@Param("status") Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status AND p.isFeatured = :isFeatured")
    Page<Product> findFeaturedProductsByParam(@Param("status") Product.ProductStatus status, @Param("isFeatured") Boolean isFeatured, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images " +
           "WHERE p.status = :status AND p.comparePrice IS NOT NULL AND p.price IS NOT NULL AND p.comparePrice > p.price")
    Page<Product> findFlashSaleProducts(@Param("status") Product.ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.slug = :slug AND p.status = :status")
    Page<Product> findByCategorySlug(@Param("slug") String slug, @Param("status") Product.ProductStatus status, Pageable pageable);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.seller LEFT JOIN FETCH p.images WHERE p.status = :status")
    List<Product> findByStatusWithImages(@Param("status") Product.ProductStatus status);
    
    long countBySellerId(String sellerId);
    long countBySellerIdAndStatus(String sellerId, Product.ProductStatus status);
    
    Optional<Product> findBySku(String sku);

    List<Product> findTop5BySellerIdOrderByTotalSoldDesc(String sellerId);

    List<Product> findTop5BySellerIdOrderByQuantityAsc(String sellerId);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category WHERE p.seller.id = :sellerId ORDER BY p.createdAt DESC")
    List<Product> findBySellerIdWithCategory(@Param("sellerId") String sellerId, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") String id);
}
