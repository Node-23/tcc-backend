package com.tcc.tccbackend.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.math.BigDecimal;

public record SaleItemDTO(
        @NotNull Long productId,
        @NotNull @PositiveOrZero int quantity,
        @NotNull @PositiveOrZero BigDecimal price
) implements Serializable {}