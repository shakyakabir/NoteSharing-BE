package com.example.notesharing.exception;

import org.springframework.http.HttpStatus;

/**
 * Carries a machine-readable error code + HTTP status so {@code GlobalExceptionHandler} can return
 * the same structured {@code data.code} shape the frontend already reads for INSUFFICIENT_AI_CREDITS.
 * Used for the subscription / feature-access gates. Never uses 401 (that would trigger the
 * frontend's token-refresh + retry) - access gates return 403, invalid input 400.
 */
public class ApiCodedException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ApiCodedException(String code, HttpStatus status, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiCodedException subscriptionRequired() {
        return new ApiCodedException("SUBSCRIPTION_REQUIRED", HttpStatus.FORBIDDEN,
                "A subscription is required to use this feature.");
    }

    public static ApiCodedException featureNotAvailable() {
        return new ApiCodedException("FEATURE_NOT_AVAILABLE", HttpStatus.FORBIDDEN,
                "This feature is not available on your current plan.");
    }

    public static ApiCodedException subscriptionExpired() {
        return new ApiCodedException("SUBSCRIPTION_EXPIRED", HttpStatus.FORBIDDEN,
                "Your subscription has expired.");
    }

    public static ApiCodedException invalidPlan() {
        return new ApiCodedException("INVALID_PLAN", HttpStatus.BAD_REQUEST,
                "The requested plan is invalid.");
    }
}
