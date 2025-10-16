package com.example.suiviexpress.Controller;

import com.example.suiviexpress.Entity.Product;
import com.example.suiviexpress.Entity.ProductCategories;
import com.example.suiviexpress.Service.ProductService;
import com.example.suiviexpress.Service.UserService;
import jdk.jfr.Category;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ✅ Get all products
    @GetMapping
    public List<Product> getAllProducts(Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.getAllProducts();
    }

    // ✅ Get by category
    @GetMapping("/category/{category}")
    public List<Product> getByCategory(@PathVariable ProductCategories category,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.getProductsByCategory(category);
    }

    // ✅ Get available products
    @GetMapping("/available")
    public List<Product> getAvailableProducts(Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.getAvailableProducts();
    }

    // ✅ Search products by keyword
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String keyword,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.searchProducts(keyword);
    }

    // ✅ Filter products by price range
    @GetMapping("/filter")
    public List<Product> filterByPrice(@RequestParam Double min, @RequestParam Double max,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.filterByPrice(min, max);
    }

    // ✅ Add product
    @PostMapping
    public Product createProduct(@RequestBody Product product,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.createProduct(product);
    }

    // ✅ Update product
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.updateProduct(id, product);
    }

    // ✅ Delete product
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        productService.deleteProduct(id);
    }

    // ✅ Update product visibility (admin action)
    @PatchMapping("/{id}/visibility")
    public Product updateVisibility(@PathVariable Long id, @RequestParam boolean visible,Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.updateProductVisibility(id, visible);
    }

    // ✅ Get only visible products
    @GetMapping("/visible")
    public List<Product> getVisibleProducts(Authentication authentication) {
        userService.verifyStuffOrAdminAccess(authentication);
        return productService.getVisibleProducts();
    }

}

