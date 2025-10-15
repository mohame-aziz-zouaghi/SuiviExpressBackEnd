package com.example.suiviexpress.Repository;

import com.example.suiviexpress.Entity.Product;
import com.example.suiviexpress.Entity.ProductCategories;
import jdk.jfr.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find by category
    List<Product> findByCategory(ProductCategories category);

    // Find by availability
    List<Product> findByAvailableTrue();

    // Find by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Optional: search by name (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);
}
