package com.example.suiviexpress.Service;


import com.example.suiviexpress.Config.JwtUtil;
import com.example.suiviexpress.DTO.AuthRequest;
import com.example.suiviexpress.DTO.AuthResponse;
import com.example.suiviexpress.DTO.RegisterRequest;
import com.example.suiviexpress.Entity.Role;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ✅ REGISTER
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .phone(request.getPhone())
                .address(request.getAddress())
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    // ✅ LOGIN
    public AuthResponse login(AuthRequest request, boolean rememberMe) {
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsernameOrEmail());
        if (optionalUser.isEmpty()) {
            optionalUser = userRepository.findByEmail(request.getUsernameOrEmail());
            if (optionalUser.isEmpty()) {
                throw new RuntimeException("User not found");
            }
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ✅ Update lastLogin
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);

        // generate JWT with userId, username, roles
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                rememberMe
        );

        return new AuthResponse(token, user.getUsername());
    }

    // ✅ Get all users (for admin)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ✅ Find by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ Delete user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
    // ✅ Enable / Disable user account
    public User setUserEnabled(Long id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ✅ Lock / Unlock user account
    public User setUserLocked(Long id, boolean locked) {
        User user = getUserById(id);
        user.setLocked(locked);
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ✅ Update user info (name, email, etc.)
    public User updateUser(Long id, User updatedUser) {
        User existingUser = getUserById(id);

        if (updatedUser.getUsername() != null) existingUser.setUsername(updatedUser.getUsername());
        if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhone() != null) existingUser.setPhone(updatedUser.getPhone());
        if (updatedUser.getRole() != null) {
            try {
                Role.valueOf(updatedUser.getRole().name());
                existingUser.setRole(updatedUser.getRole());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role value: " + updatedUser.getRole());
            }
        }
        if (updatedUser.getFirstName() != null) existingUser.setFirstName(updatedUser.getFirstName());
        if (updatedUser.getLastName() != null) existingUser.setLastName(updatedUser.getLastName());
        if (updatedUser.getAddress() != null) existingUser.setAddress(updatedUser.getAddress());
        if (updatedUser.getPassword() != null) existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }





    // 🔒 Generic method to get the current authenticated user
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Unauthorized: Authentication missing");
        }

        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // 🚫 Prevent any action if the account is locked
        if (currentUser.isLocked()) {
            throw new RuntimeException("Account locked. Access denied.");
        }

        return currentUser;
    }


    // 🔒 Verify admin access
    public void verifyAdminAccess(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (!currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new RuntimeException("Access denied: Admins only");
        }
    }


    // 🔒 Verify admin access
    public void verifyStuffOrAdminAccess(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        if (!currentUser.getRole().equals(Role.ROLE_STUFF) && !currentUser.getRole().equals(Role.ROLE_ADMIN) ) {
            throw new RuntimeException("Access denied: Admins Or Stuff only");
        }
    }

    // 🔒 Verify admin or self access
    public void verifyAdminOrSelf(Authentication authentication, Long targetUserId) {
        User currentUser = getCurrentUser(authentication);

        if (!currentUser.getRole().equals(Role.ROLE_ADMIN) && !currentUser.getId().equals(targetUserId)) {
            throw new RuntimeException("Access denied: insufficient permissions");
        }
    }
}

