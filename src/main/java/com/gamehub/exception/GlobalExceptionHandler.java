package com.gamehub.exception;

import com.gamehub.auth.exception.EmailExistsException;
import com.gamehub.auth.exception.InvalidCredentialsException;
import com.gamehub.auth.exception.UsernameExistsException;
import com.gamehub.game.exception.*;
import com.gamehub.room.exception.*;
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

    @ExceptionHandler(GameExistsException.class)
    public ResponseEntity<ErrorResponse> handleGameExists(GameExistsException ex , HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                "Game already exists",
                409,
                request.getRequestURI(),
                "Game you are trying to input already exists"
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(InvalidPlayerRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPlayerRange(InvalidPlayerRangeException ex , HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid Player Range",
                410,
                request.getRequestURI(),
                "Minimum players cannot be greater than maximum players"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(GameNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleGameNotExists(GameNotExistsException ex , HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                "Game not Exists",
                410,
                request.getRequestURI(),
                "Game not Found!"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RoomNotExistsException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotExists(RoomNotExistsException ex , HttpServletRequest request){
    ErrorResponse errorResponse = new ErrorResponse(
            ex.getMessage(),
            404,
            request.getRequestURI(),
        "Room not Exists!"
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(RoomNotWaitingException.class)
    public ResponseEntity<ErrorResponse> handleRoomNotWaiting(RoomNotWaitingException ex , HttpServletRequest request){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                410,
                request.getRequestURI(),
                "Cant join Room"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RoomFullException.class)
    public ResponseEntity<ErrorResponse> handleRoomFull(RoomFullException ex , HttpServletRequest req){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                410,
                req.getRequestURI(),
                "Cant join room"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(PlayerAlreadyInRoomException.class)
    public ResponseEntity<ErrorResponse> handlePlayerAlreadyInRoom(
            PlayerAlreadyInRoomException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                410,
                request.getRequestURI(),
                "Cant Join Room"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(PlayerNotInRoomException.class)
    public ResponseEntity<ErrorResponse> handlePlayerNotInRoom(
            PlayerNotInRoomException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                410,
                request.getRequestURI(),
                "Player not in the Room"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                401,
                request.getRequestURI(),
                "Unauthorized Access"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InsufficientPlayersException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientPlayers(
            InsufficientPlayersException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                410,
                request.getRequestURI(),
                "Insufficient Players to Start Game"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RoleAllocationException.class)
    public ResponseEntity<ErrorResponse> handleRoleAllocation(
            RoleAllocationException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                500,
                request.getRequestURI(),
                "RoleAllocation Error"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(InvalidRoleConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRoleConfiguration(
            InvalidRoleConfigurationException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                400,
                request.getRequestURI(),
                "InvalidRoleConfiguration Error"
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidPlayerCountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPlayerCount(
            InvalidPlayerCountException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                400,
                request.getRequestURI(),
                "InvalidPlayerCount"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(GameSessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameSessionNotFound(
            GameSessionNotFoundException ex,
            HttpServletRequest request
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                404,
                request.getRequestURI(),
                "GameSessionNotFound"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }





}
