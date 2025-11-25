package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Complaint;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {
    List<Complaint> findByStatus(String status, Sort sort);
    List<Complaint> findByReporterId(String reporterId, Sort sort);
    List<Complaint> findByReporterIdAndStatus(String reporterId, String status, Sort sort);
    List<Complaint> findByTargetId(String targetId, Sort sort);
    List<Complaint> findByTargetIdAndStatus(String targetId, String status, Sort sort);
}


