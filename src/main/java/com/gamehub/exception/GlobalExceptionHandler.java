package com.gamehub.exception;

import com.gamehub.auth.exception.EmailExistsException;
import com.gamehub.auth.exception.UsernameExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameExists(UsernameExistsException ex , HttpServletRequest request){

        ErrorResponse errorResponse = new ErrorResponse(
                "Username already exists",
                409,
                request.getRequestURI(),
                "Username you are trying to input already exists"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(EmailExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailExists(EmailExistsException ex, HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                "Email already exists",
                409,
                request.getRequestURI(),
                "Email you are trying to input already exists"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        
    }
}
