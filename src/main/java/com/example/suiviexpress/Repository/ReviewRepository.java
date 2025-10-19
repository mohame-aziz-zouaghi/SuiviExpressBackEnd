package com.example.suiviexpress.Repository;


import com.example.suiviexpress.Entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductId(Long productId);
    List<Review> findByUserId(Long userId);
    long countByProductIdAndUserId(Long productId, Long userId);

}

