package com.example.suiviexpress.Repository;

import com.example.suiviexpress.Entity.Payment;
import com.example.suiviexpress.Entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentIntentId(String paymentIntentId);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId,
                                        @Param("status") PaymentStatus status);

    boolean existsByPaymentIntentId(String paymentIntentId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCEEDED'")
    Optional<Double> getTotalRevenue();
}