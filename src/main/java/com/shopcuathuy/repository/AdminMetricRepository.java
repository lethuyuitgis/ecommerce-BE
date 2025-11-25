package com.shopcuathuy.repository;

import com.shopcuathuy.entity.AdminMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminMetricRepository extends JpaRepository<AdminMetric, String> {
}


