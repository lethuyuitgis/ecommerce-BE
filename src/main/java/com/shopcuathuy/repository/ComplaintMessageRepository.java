package com.shopcuathuy.repository;

import com.shopcuathuy.entity.ComplaintMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintMessageRepository extends JpaRepository<ComplaintMessage, String> {
    List<ComplaintMessage> findByComplaintIdOrderByCreatedAtAsc(String complaintId);
}





