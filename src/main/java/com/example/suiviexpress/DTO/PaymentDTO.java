package com.example.suiviexpress.DTO;

import com.example.suiviexpress.Entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private Double amount;
    private String currency;
    private PaymentStatus status;
    private String description;
    private String paymentMethod;
    private Long userId;
    private String userName;
    private String userEmail;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String paymentIntentId;
}