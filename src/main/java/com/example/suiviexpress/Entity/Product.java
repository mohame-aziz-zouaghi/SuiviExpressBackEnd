package com.example.suiviexpress.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🏷️ Basic Info
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private ProductCategories category;

    // 💰 Pricing
    @Column(nullable = false)
    private Double price;

    private Double discount; // percentage or amount

    @Column(nullable = false)
    // 📦 Stock & Availability
    private int stockQuantity;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private boolean Visible = false;



    @Column(nullable = false)
    // 🖼️ Media
    private String imageUrl;

    private String thumbnailUrl;

    // ⭐ Ratings & Popularity
    private Double averageRating = 0.0;
    private int reviewCount = 0;

    // ⏰ Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


