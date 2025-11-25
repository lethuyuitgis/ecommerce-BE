package com.shopcuathuy.repository;

import com.shopcuathuy.entity.Voucher;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    // Find vouchers by seller
    Page<Voucher> findBySellerIdOrderByCreatedAtDesc(String sellerId, Pageable pageable);
    
    // Find available vouchers (active, not expired, within date range)
    @Query("SELECT v FROM Voucher v WHERE v.status = 'ACTIVE' " +
           "AND v.startDate <= :now AND v.endDate >= :now " +
           "AND (v.seller IS NULL OR v.seller.id = :sellerId) " +
           "AND (v.minPurchaseAmount IS NULL OR v.minPurchaseAmount <= :subtotal) " +
           "AND (v.totalUsesLimit IS NULL OR v.totalUses < v.totalUsesLimit)")
    List<Voucher> findAvailableVouchers(@Param("now") LocalDateTime now,
                                        @Param("sellerId") String sellerId,
                                        @Param("subtotal") java.math.BigDecimal subtotal);
}

