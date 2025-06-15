package com.tcc.tccbackend.DTO;

import com.tcc.tccbackend.Model.Customer;
import com.tcc.tccbackend.Model.Employee;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OutputSaleDTO(
        Long id,
        @NotNull Customer client,
        @NotNull Employee employee,
        @NotNull LocalDateTime date,
        @NotBlank String paymentMethod,
        @NotBlank String status,
        @NotBlank BigDecimal total,
        @Valid @NotNull List<SaleItemDTO> items
) implements Serializable {}