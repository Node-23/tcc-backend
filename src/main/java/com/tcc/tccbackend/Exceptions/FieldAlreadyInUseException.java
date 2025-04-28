package com.tcc.tccbackend.Exceptions;

public class FieldAlreadyInUseException extends RuntimeException {

    public FieldAlreadyInUseException(String field) {
        super(field + " já cadastrado");
    }
}
