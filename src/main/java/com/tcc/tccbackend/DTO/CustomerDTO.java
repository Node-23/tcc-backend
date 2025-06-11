package com.tcc.tccbackend.DTO;

import java.io.Serializable;

public record CustomerDTO(
        Long id,
        String name,
        String email,
        String phone,
        String address
) implements Serializable {}