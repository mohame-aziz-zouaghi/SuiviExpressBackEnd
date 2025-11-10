package com.example.suiviexpress.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private Double amount;
    private String currency;
    private String redirectUrl;
}
