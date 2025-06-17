package com.hostel.management.repository;

import com.hostel.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBillBillIdOrderByCreatedAtDesc(Long billId);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.bill.billId = :billId")
    BigDecimal sumAmountByBillId(@Param("billId") Long billId);
}
