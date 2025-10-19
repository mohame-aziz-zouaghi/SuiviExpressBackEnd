package com.example.suiviexpress.DTO;

import lombok.Data;

@Data
public class ReviewSummaryDTO {
    private int rating;
    private String comment;
    private Long productId;
    private Long userId;
}
