package com.tcc.tccbackend.DTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeDTO(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String role,
        BigDecimal salary,
        LocalDate hiredate,
        String status
) implements Serializable {}