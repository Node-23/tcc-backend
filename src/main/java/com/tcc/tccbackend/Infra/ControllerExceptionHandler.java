package com.tcc.tccbackend.Infra;

import com.tcc.tccbackend.DTO.ExceptionDTO;
import com.tcc.tccbackend.Exceptions.FieldAlreadyInUseException;
import com.tcc.tccbackend.Exceptions.InvalidDataException;
import com.tcc.tccbackend.Exceptions.InvalidEmailException;
import com.tcc.tccbackend.Exceptions.PasswordRulesException;
import com.tcc.tccbackend.Service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private final LogService logService;

    public ControllerExceptionHandler(LogService logService) {
        this.logService = logService;
    }

    @ExceptionHandler(PasswordRulesException.class)
    public ResponseEntity<ExceptionDTO> handlePasswordRulesException(PasswordRulesException exception) {
        logService.warn("Password rules violation: " + exception.getMessage(), "UserRegistration", exception.getMessage());
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ExceptionDTO> handleInvalidEmailException(InvalidEmailException exception) {
        logService.warn("Invalid email format: " + exception.getMessage(), "UserRegistration", exception.getMessage());
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(FieldAlreadyInUseException.class)
    public ResponseEntity<ExceptionDTO> handleFieldAlreadyInUseException(FieldAlreadyInUseException exception) {
        logService.warn("Field already in use: " + exception.getMessage(), "DataIntegrity", exception.getMessage());
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ExceptionDTO> handleInvalidDataException(InvalidDataException exception) {
        logService.warn("Invalid data provided: " + exception.getMessage(), "Validation", exception.getMessage());
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(Exception exception) {
        logService.error("Unhandled internal server error: " + exception.getMessage(), "GlobalExceptionHandler", Arrays.toString(exception.getStackTrace()));
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "500");
        return ResponseEntity.internalServerError().body(exceptionDTO);
    }
}