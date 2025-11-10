package com.example.suiviexpress.Controller;

import com.example.suiviexpress.DTO.ProductDTO;
import com.example.suiviexpress.Entity.Product;
import com.example.suiviexpress.Entity.ProductCategories;
import com.example.suiviexpress.Service.ProductService;
import com.example.suiviexpress.Service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.*;


import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // allow mobile frontend access
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    public ProductController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    // -------------------- Utility to convert Product to DTO --------------------
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .category(product.getCategory())
                .price(product.getPrice())
                .discount(product.getDiscount())
                .stockQuantity(product.getStockQuantity())
                .available(product.isAvailable())
                .visible(product.isVisible())
                .imageUrl(product.getImageUrl())
                .thumbnailUrl(product.getThumbnailUrl())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .build();
    }

    // ✅ Get all products
    @GetMapping
    public List<ProductDTO> getAllProducts(Authentication authentication) {
        return productService.getAllProducts()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get by category
    @GetMapping("/category/{category}")
    public List<ProductDTO> getByCategory(@PathVariable ProductCategories category, Authentication authentication) {
        return productService.getProductsByCategory(category)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Get available products
    @GetMapping("/available")
    public List<ProductDTO> getAvailableProducts(Authentication authentication) {
        return productService.getAvailableProducts()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Search products by keyword
    @GetMapping("/search")
    public List<ProductDTO> searchProducts(@RequestParam String keyword, Authentication authentication) {
        return productService.searchProducts(keyword)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Filter products by price range
    @GetMapping("/filter")
    public List<ProductDTO> filterByPrice(@RequestParam Double min, @RequestParam Double max, Authentication authentication) {
        return productService.filterByPrice(min, max)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ Add product
    @PostMapping
    public ProductDTO createProduct(@RequestBody Product product, Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return toDTO(productService.createProduct(product));
    }

    // ✅ Update product
    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable Long id, @RequestBody Product product, Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return toDTO(productService.updateProduct(id, product));
    }

    // ✅ Delete product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id, Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        productService.deleteProduct(id);
    }

    // ✅ Update product visibility (admin action)
    @PatchMapping("/{id}/visibility")
    public ProductDTO updateVisibility(@PathVariable Long id, @RequestParam boolean visible, Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return toDTO(productService.updateProductVisibility(id, visible));
    }

    // ✅ Get only visible products
    @GetMapping("/visible")
    public List<ProductDTO> getVisibleProducts(Authentication authentication) {
        return productService.getVisibleProducts()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
