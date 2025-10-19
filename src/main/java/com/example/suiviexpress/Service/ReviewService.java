package com.example.suiviexpress.Service;





import com.example.suiviexpress.Entity.Product;
import com.example.suiviexpress.Entity.Review;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Repository.ProductRepository;
import com.example.suiviexpress.Repository.ReviewRepository;
import com.example.suiviexpress.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // -------------------- Get All Reviews --------------------
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // -------------------- Get Reviews by Product --------------------
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    // -------------------- Get Reviews by User --------------------
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    // -------------------- Get Review by ID --------------------
    public Optional<Review> getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    // -------------------- Create Review --------------------
    public Review createReview(Long productId, Long userId, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if user already submitted 2 reviews for this product
        long existingReviewsCount = reviewRepository.countByProductIdAndUserId(productId, userId);
        if (existingReviewsCount >= 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each user can review only twice");
        }

        review.setProduct(product);
        review.setUser(user);
        review.setCreatedAt(java.time.LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Update product average rating dynamically
        updateProductRating(productId);

        return savedReview;
    }


    // -------------------- Update Review --------------------
    public Review updateReview(Long reviewId, Long productId, Review updatedReview) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        if (updatedReview.getRating() != existingReview.getRating()) {
            existingReview.setRating(updatedReview.getRating());
        }
        if (updatedReview.getComment() != null) {
            existingReview.setComment(updatedReview.getComment());
        }

        Review savedReview = reviewRepository.save(existingReview);

        // Recalculate product rating after update
        updateProductRating(productId);

        return savedReview;
    }

    // -------------------- Delete Review --------------------
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        Long productId = review.getProduct().getId();

        reviewRepository.delete(review);

        // Recalculate product rating after deletion
        updateProductRating(productId);
    }

    // -------------------- Helper: Update Product Rating --------------------
    private void updateProductRating(Long productId) {
        List<Review> productReviews = reviewRepository.findByProductId(productId);

        double average = 0.0;
        if (!productReviews.isEmpty()) {
            average = productReviews.stream()
                    .mapToDouble(Review::getRating)
                    .average()
                    .orElse(0.0);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        product.setAverageRating(average);
        product.setReviewCount(productReviews.size());

        productRepository.save(product);
    }
}
