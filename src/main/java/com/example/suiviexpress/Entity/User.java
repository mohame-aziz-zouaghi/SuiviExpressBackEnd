package com.example.suiviexpress.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 150)
    private String firstName;

    @Column(nullable = false, unique = true, length = 150)
    private String lastName;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String address;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean locked = false;

    @Column(nullable = false)
    private LocalDateTime createdAt=LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt=LocalDateTime.now();

    private LocalDateTime lastLogin;

    @Column(length = 1000)
    private String deviceToken;
}
