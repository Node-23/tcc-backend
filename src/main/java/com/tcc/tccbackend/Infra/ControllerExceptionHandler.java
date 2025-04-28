package com.tcc.tccbackend.Infra;

import com.tcc.tccbackend.DTO.ExceptionDTO;
import com.tcc.tccbackend.Exceptions.FieldAlreadyInUseException;
import com.tcc.tccbackend.Exceptions.InvalidEmailException;
import com.tcc.tccbackend.Exceptions.PasswordRulesException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(PasswordRulesException.class)
    public ResponseEntity<ExceptionDTO> handlePasswordRulesException(PasswordRulesException exception) {
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<ExceptionDTO> handleUserNotFoundException(UserNotFoundException exception) {
//        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "404");
//        return ResponseEntity.status(404).body(exceptionDTO);
//    }

    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<ExceptionDTO> handleInvalidEmailException(InvalidEmailException exception) {
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(FieldAlreadyInUseException.class)
    public ResponseEntity<ExceptionDTO> handleFieldAlreadyInUseException(FieldAlreadyInUseException exception) {
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "400");
        return ResponseEntity.badRequest().body(exceptionDTO);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionDTO> handleGeneralException(Exception exception) {
        ExceptionDTO exceptionDTO = new ExceptionDTO(exception.getMessage(), "500");
        return ResponseEntity.internalServerError().body(exceptionDTO);
    }
}
