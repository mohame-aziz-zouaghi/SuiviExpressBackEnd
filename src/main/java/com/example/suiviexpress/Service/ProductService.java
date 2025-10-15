package com.example.suiviexpress.Service;

import com.example.suiviexpress.Entity.Product;
import com.example.suiviexpress.Entity.ProductCategories;
import com.example.suiviexpress.Repository.ProductRepository;
import jdk.jfr.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // ✅ Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // ✅ Get products by category
    public List<Product> getProductsByCategory(ProductCategories category) {
        return productRepository.findByCategory(category);
    }

    // ✅ Get available products
    public List<Product> getAvailableProducts() {
        return productRepository.findByAvailableTrue();
    }

    // ✅ Search by name
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    // ✅ Filter by price range
    public List<Product> filterByPrice(Double min, Double max) {
        return productRepository.findByPriceBetween(min, max);
    }

    // ✅ Create product
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // ✅ Update product
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedProduct.getName());
                    existing.setDescription(updatedProduct.getDescription());
                    existing.setBrand(updatedProduct.getBrand());
                    existing.setCategory(updatedProduct.getCategory());
                    existing.setPrice(updatedProduct.getPrice());
                    existing.setDiscount(updatedProduct.getDiscount());
                    existing.setStockQuantity(updatedProduct.getStockQuantity());
                    existing.setAvailable(updatedProduct.isAvailable());
                    existing.setImageUrl(updatedProduct.getImageUrl());
                    existing.setThumbnailUrl(updatedProduct.getThumbnailUrl());
                    existing.setAverageRating(updatedProduct.getAverageRating());
                    existing.setReviewCount(updatedProduct.getReviewCount());
                    return productRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    // ✅ Delete product
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id " + id);
        }
        productRepository.deleteById(id);
    }

    // ✅ Update product visibility
    public Product updateProductVisibility(Long id, boolean visible) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setVisible(visible);
                    return productRepository.save(product);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    // ✅ Get only visible products
    public List<Product> getVisibleProducts() {
        return productRepository.findAll()
                .stream()
                .filter(Product::isVisible)
                .toList();
    }


}
