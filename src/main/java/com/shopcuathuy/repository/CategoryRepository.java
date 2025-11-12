package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Category;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findBySlug(String slug);
    
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<Category> findByParentIsNullAndIsActiveTrue();
    
    List<Category> findByParentIdAndIsActiveTrue(String parentId);
    
    Optional<Category> findByNameIgnoreCase(String name);
}

