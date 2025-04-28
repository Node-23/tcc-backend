package com.tcc.tccbackend.DTO;

import java.io.Serializable;

public record LoginDTO(
        String email,
        String password
) implements Serializable {}