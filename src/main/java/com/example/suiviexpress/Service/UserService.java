package com.example.suiviexpress.Service;


import com.example.suiviexpress.Config.JwtUtil;
import com.example.suiviexpress.DTO.AuthRequest;
import com.example.suiviexpress.DTO.AuthResponse;
import com.example.suiviexpress.DTO.RegisterRequest;
import com.example.suiviexpress.Entity.Role;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Repository.UserRepository;
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
        userRepository.deleteById(id);
    }
}

