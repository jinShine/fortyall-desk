package com.buzz.fortyall_desk.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public final class ProductDto {
    private ProductDto() {}

    public record CreateRequest(@NotBlank String name,
                                @Positive int durationMinutes,
                                @Positive int capacity,
                                @Positive int sessionCount,
                                @Positive int validDays,
                                @Positive BigDecimal price,
                                String description) {}

    public record ProductView(Long productId, String name, int durationMinutes, int capacity,
                              int sessionCount, int validDays, BigDecimal price,
                              String description) {}
}
