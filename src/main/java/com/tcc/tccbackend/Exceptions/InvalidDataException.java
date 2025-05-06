package com.tcc.tccbackend.Exceptions;

public class InvalidDataException extends RuntimeException{
    public InvalidDataException(String msg) {
        super(msg);
    }
}
