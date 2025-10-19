package com.example.suiviexpress.DTO;

import com.example.suiviexpress.Entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String profileImageUrl;
    private Role role;
    private boolean enabled;
    private boolean locked;
}
