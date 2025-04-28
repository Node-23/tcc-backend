package com.tcc.tccbackend.DTO;

import java.io.Serializable;

public record UserDTO(
        Long id,
        String name,
        String email,
        String password
) implements Serializable {}
