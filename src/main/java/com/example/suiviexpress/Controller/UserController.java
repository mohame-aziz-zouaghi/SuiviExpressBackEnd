package com.example.suiviexpress.Controller;

import com.example.suiviexpress.Entity.Role;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Service.UserService;
import com.example.suiviexpress.Config.JwtUtil;
import org.springframework.security.core.Authentication;
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

    // ✅ Get all users (Admin only)
    @GetMapping
    public List<User> getAllUsers(Authentication authentication) {
        userService.verifyAdminAccess(authentication);
        return userService.getAllUsers();
    }

    // ✅ Get single user (Admin or owner)
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id, Authentication authentication) {
        userService.verifyAdminOrSelf(authentication, id);
        return userService.getUserById(id);
    }

    // ✅ Update user info (Admin or owner)
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,
                           @RequestBody User updatedUser,
                           Authentication authentication) {
        userService.verifyAdminOrSelf(authentication, id);
        return userService.updateUser(id, updatedUser);
    }

    // ✅ Enable or disable user account (Admin only)
    @PutMapping("/{id}/enable")
    public User setUserEnabled(@PathVariable Long id,
                               @RequestParam boolean enabled,
                               Authentication authentication) {
        userService.verifyAdminAccess(authentication);
        return userService.setUserEnabled(id, enabled);
    }

    // ✅ Lock or unlock user account (Admin only)
    @PutMapping("/{id}/lock")
    public User setUserLocked(@PathVariable Long id,
                              @RequestParam boolean locked,
                              Authentication authentication) {
        userService.verifyAdminAccess(authentication);
        return userService.setUserLocked(id, locked);
    }

    // ✅ Delete user (Admin only)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        userService.verifyAdminOrSelf(authentication,id);
        userService.deleteUser(id);
    }
}
