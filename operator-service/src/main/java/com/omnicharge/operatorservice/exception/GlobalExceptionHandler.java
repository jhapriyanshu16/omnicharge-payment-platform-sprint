package com.omnicharge.operatorservice.exception;

import com.omnicharge.operatorservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OperatorAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleExists(OperatorAlreadyExistsException ex){
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(OperatorNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(OperatorNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex){
        String msg = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message(msg)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex){
        ex.printStackTrace();
        return ResponseEntity.internalServerError().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Internal Server Error")
                        .build()
        );
    }
}