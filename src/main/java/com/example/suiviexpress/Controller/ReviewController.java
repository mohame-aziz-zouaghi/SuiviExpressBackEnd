package com.example.suiviexpress.Controller;


import com.example.suiviexpress.DTO.ReviewDTO;
import com.example.suiviexpress.Entity.Review;
import com.example.suiviexpress.Service.ReviewService;
import com.example.suiviexpress.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // Convert Review entity to DTO
    private ReviewDTO toDTO(Review review) {
        return new ReviewDTO(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getUser().getId(),
                review.getProduct().getId(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }

    // -------------------- Get All Reviews --------------------
    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> dtos = reviewService.getAllReviews()
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // -------------------- Get Reviews by Product --------------------
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByProduct(@PathVariable Long productId) {
        List<ReviewDTO> dtos = reviewService.getReviewsByProduct(productId)
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // -------------------- Get Reviews by User --------------------
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        userService.verifyAdminOrSelf(authentication, userId);
        List<ReviewDTO> dtos = reviewService.getReviewsByUser(userId)
                .stream()
                .map(this::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // -------------------- Get Review by ID --------------------
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Long reviewId) {
        return reviewService.getReviewById(reviewId)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------- Create Review --------------------
    @PostMapping("/user/{userId}/product/{productId}")
    public ResponseEntity<ReviewDTO> createReview(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody Review review,
            Authentication authentication
    ) {
        Review savedReview = reviewService.createReview(productId, userId, review);
        return ResponseEntity.ok(toDTO(savedReview));
    }

    // -------------------- Update Review --------------------
    @PutMapping("/{reviewId}/user/{userId}/product/{productId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @PathVariable Long reviewId,
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody Review updatedReview,
            Authentication authentication
    ) {
        userService.verifyAdminOrSelf(authentication, userId);
        Review savedReview = reviewService.updateReview(reviewId, productId, updatedReview);
        return ResponseEntity.ok(toDTO(savedReview));
    }

    // -------------------- Delete Review --------------------
    @DeleteMapping("/{reviewId}/user/{userId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @PathVariable Long userId,
            Authentication authentication
    ) {
        userService.verifyAdminOrSelf(authentication, userId);
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
