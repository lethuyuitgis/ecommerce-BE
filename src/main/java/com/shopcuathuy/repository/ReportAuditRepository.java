package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ReportAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportAuditRepository extends JpaRepository<ReportAudit, String> {
}



