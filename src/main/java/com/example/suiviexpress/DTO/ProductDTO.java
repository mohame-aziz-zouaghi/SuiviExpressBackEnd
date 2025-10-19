package com.example.suiviexpress.DTO;

import com.example.suiviexpress.Entity.ProductCategories;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private ProductCategories category;
    private Double price;
    private Double discount;
    private int stockQuantity;
    private boolean available;
    private boolean visible;
    private String imageUrl;
    private String thumbnailUrl;
    private Double averageRating;
    private int reviewCount;
}

