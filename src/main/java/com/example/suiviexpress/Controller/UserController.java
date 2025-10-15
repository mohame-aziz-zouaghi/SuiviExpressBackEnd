package com.example.suiviexpress.Controller;

import com.example.suiviexpress.Entity.Role;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Service.UserService;
import com.example.suiviexpress.Config.JwtUtil;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // ✅ Helper method to get user from JWT
    private User getCurrentUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        if (!jwtUtil.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        return userService.getUserById(userId); // fetch fresh user from DB
    }

    // ✅ Get all users (admin only)
    @GetMapping
    public List<User> getAllUsers(HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access denied: Admins only");
        }
        return userService.getAllUsers();
    }

    // ✅ Get single user by ID (admin or self)
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRole() != Role.ROLE_ADMIN && !currentUser.getId().equals(id)) {
            throw new RuntimeException("Access denied");
        }
        return userService.getUserById(id);
    }

    // ✅ Delete user (admin only)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Access denied: Admins only");
        }
        userService.deleteUser(id);
    }
}
