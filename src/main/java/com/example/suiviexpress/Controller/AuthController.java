package com.example.suiviexpress.Controller;

import com.example.suiviexpress.DTO.AuthRequest;
import com.example.suiviexpress.DTO.AuthResponse;
import com.example.suiviexpress.DTO.RegisterRequest;
import com.example.suiviexpress.Entity.User;
import com.example.suiviexpress.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public User register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request,
                              @RequestParam(defaultValue = "false") boolean rememberMe) {
        return userService.login(request, rememberMe);
    }
}

