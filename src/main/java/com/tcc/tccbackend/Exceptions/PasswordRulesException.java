package com.tcc.tccbackend.Exceptions;

public class PasswordRulesException extends RuntimeException{
    public PasswordRulesException(String message) {
        super(message);
    }
}
