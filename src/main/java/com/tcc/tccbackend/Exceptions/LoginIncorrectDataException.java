package com.tcc.tccbackend.Exceptions;

public class LoginIncorrectDataException extends RuntimeException{
    public LoginIncorrectDataException() {
        super("Email ou senha incorretos");
    }
}
