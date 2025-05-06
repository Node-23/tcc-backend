package com.tcc.tccbackend.DTO;

import java.io.Serializable;
import java.math.BigDecimal;

public record ProductDTO(
        Long id,
        Long ownerId,
        String name,
        String code,
        String category,
        BigDecimal price,
        BigDecimal profit,
        int quantity
) implements Serializable {}
