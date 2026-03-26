package com.omnicharge.operatorservice.exception;

import com.omnicharge.operatorservice.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ⭐ Operator Exceptions

    @ExceptionHandler(OperatorAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handleOperatorExists(OperatorAlreadyExistsException ex){
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(OperatorNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleOperatorNotFound(OperatorNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

    // ⭐ Plan Exceptions

    @ExceptionHandler(PlanAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<?>> handlePlanExists(PlanAlreadyExistsException ex){
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handlePlanNotFound(PlanNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build()
        );
    }

    // ⭐ Validation Errors

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException ex){

        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message(msg)
                        .build()
        );
    }

    // ⭐ Invalid JSON Body

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidJson(HttpMessageNotReadableException ex){
        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Invalid request body")
                        .build()
        );
    }

    // ⭐ Database Constraint Errors

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDbError(DataIntegrityViolationException ex){
        log.error("Database error", ex);

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Database constraint violation")
                        .build()
        );
    }

    // ⭐ Generic Fallback

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex){
        log.error("Unhandled exception", ex);

        return ResponseEntity.internalServerError().body(
                ApiResponse.builder()
                        .success(false)
                        .message("Internal Server Error")
                        .build()
        );
    }
}