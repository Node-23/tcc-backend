package com.tcc.tccbackend.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record SaleDTO(
        Long id,
        @NotNull Long clientId,
        @NotNull Long employeeId,
        @NotNull LocalDateTime date,
        @NotBlank String paymentMethod,
        @NotBlank String status,
        @Valid @NotNull List<SaleItemDTO> items
) implements Serializable {}