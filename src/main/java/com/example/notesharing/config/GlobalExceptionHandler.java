package com.example.notesharing.config;

import com.example.notesharing.DTO.InsufficientCreditsResponse;
import com.example.notesharing.exception.InsufficientCreditsException;
import com.example.notesharing.payload.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Insufficient AI credits gets its own handler so the response carries a machine-readable code
     * plus the required/available numbers in ApiResponse.data (HTTP 402). More specific than the
     * generic RuntimeException handler below, so Spring routes InsufficientCreditsException here.
     */
    @ExceptionHandler(InsufficientCreditsException.class)
    public ResponseEntity<ApiResponse<?>> handleInsufficientCredits(InsufficientCreditsException exception) {
        InsufficientCreditsResponse body = new InsufficientCreditsResponse(
                "INSUFFICIENT_AI_CREDITS",
                exception.getRequiredCredits(),
                exception.getAvailableCredits());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.builder()
                        .status("402")
                        .message(exception.getMessage())
                        .data(body)
                        .build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .status("400")
                        .message(exception.getMessage())
                        .data(null)
                        .build());
    }
}
