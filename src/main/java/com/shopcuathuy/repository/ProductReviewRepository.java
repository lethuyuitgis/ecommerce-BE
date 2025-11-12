package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, String> {
    Page<ProductReview> findByProductIdAndStatus(String productId, ProductReview.ReviewStatus status, Pageable pageable);
    Page<ProductReview> findByUserId(String userId, Pageable pageable);
}

