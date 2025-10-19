package com.example.suiviexpress.Controller;

import com.example.suiviexpress.DTO.UserDTO;
import com.example.suiviexpress.Entity.Role;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Service.UserService;
import com.example.suiviexpress.Config.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

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

    // -------------------- Get All Users --------------------
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> dtos = userService.getAllUsers()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // -------------------- Get User by ID --------------------
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId); // returns User or null

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toDTO(user));
    }


    // -------------------- Update User --------------------
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody User updatedUser,
            Authentication authentication
    ) {
        userService.verifyAdminOrSelf(authentication, userId);
        User savedUser = userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(toDTO(savedUser));
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

    // -------------------- Delete User --------------------
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId,
            Authentication authentication
    ) {
        userService.verifyAdminOrSelf(authentication, userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    // -------------------- Helper Method --------------------
    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .build();
    }
}
