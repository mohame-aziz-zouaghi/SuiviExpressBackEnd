package com.example.suiviexpress.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentDTO {
    private Double amount;
    private String currency;
    private String description;
    private String paymentMethod;
}
