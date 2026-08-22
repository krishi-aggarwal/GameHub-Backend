package com.gamehub.exception;

import com.gamehub.auth.exception.EmailExistsException;
import com.gamehub.auth.exception.InvalidCredentialsException;
import com.gamehub.auth.exception.UsernameExistsException;
import com.gamehub.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.validation.BindingResultUtils.getBindingResult;

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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex , HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid Credentials",
                401,
                request.getRequestURI(),
                "Invalid Username or password"
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ValidationError> validationErrorList = new ArrayList<>();


        for(FieldError err : fieldErrors){
            validationErrorList.add(new ValidationError(err.getField(),err.getDefaultMessage()));
        }

        ErrorResponse errorResponse = new ErrorResponse(
                "Validation failed",
                400,
                request.getRequestURI(),
                "Provide valid input"
        );
            errorResponse.setErrors(validationErrorList);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex , HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                "User not Found",
                404,
                request.getRequestURI(),
                "User not Found!"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


}
