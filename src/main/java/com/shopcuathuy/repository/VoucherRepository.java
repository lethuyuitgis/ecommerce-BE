package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Voucher;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);
    Optional<Voucher> findByCodeAndStatus(String code, Voucher.VoucherStatus status);
    
    // Find active vouchers
    Optional<Voucher> findByCodeAndStatusAndStartDateBeforeAndEndDateAfter(
        String code, 
        Voucher.VoucherStatus status,
        LocalDateTime now1,
        LocalDateTime now2
    );
}

