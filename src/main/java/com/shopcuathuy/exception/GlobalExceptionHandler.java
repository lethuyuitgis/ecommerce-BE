package com.shopcuathuy.exception;

import com.shopcuathuy.api.ApiResponse;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed: " + errors.toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwt(ExpiredJwtException ex) {
        // JWT expired - return 401 but don't log as error (it's expected)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Token expired. Please login again."));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException ex) {
        // Other JWT errors - return 401
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Invalid token: " + ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Invalid request body format";
        
        // Check if it's a JSON parsing error
        Throwable cause = ex.getCause();
        if (cause instanceof MismatchedInputException) {
            MismatchedInputException mismatchEx = (MismatchedInputException) cause;
            String mismatchMessage = mismatchEx.getMessage();
            if (mismatchMessage != null) {
                if (mismatchMessage.contains("START_ARRAY") && mismatchMessage.contains("LinkedHashMap")) {
                    message = "Expected object but received array. Please send a JSON object, not an array. " +
                             "Example: {\"field\": \"value\"} instead of [{\"field\": \"value\"}]";
                } else if (mismatchMessage.contains("START_OBJECT") && mismatchMessage.contains("List")) {
                    message = "Expected array but received object. Please send a JSON array, not an object. " +
                             "Example: [{\"field\": \"value\"}] instead of {\"field\": \"value\"}";
                } else if (mismatchMessage.contains("LinkedHashMap")) {
                    // Try to extract the target type from the error message
                    String targetType = extractTargetType(mismatchMessage);
                    message = "JSON format error: Expected " + targetType + " but received different format. " +
                             mismatchMessage;
                } else {
                    message = "JSON format error: " + mismatchMessage;
                }
            }
        } else if (cause != null) {
            message = "Request body parsing error: " + cause.getMessage();
        }
        
        // Log the full exception for debugging
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest httpRequest = attributes.getRequest();
                String method = httpRequest.getMethod();
                String uri = httpRequest.getRequestURI();
                String queryString = httpRequest.getQueryString();
                String fullUrl = uri + (queryString != null ? "?" + queryString : "");
                System.err.println("=== JSON Parsing Error ===");
                System.err.println("Method: " + method);
                System.err.println("URL: " + fullUrl);
                System.err.println("Exception: " + ex.getMessage());
            }
        } catch (Exception e) {
            // Ignore if can't get request info
        }
        
        System.err.println("HttpMessageNotReadableException: " + ex.getMessage());
        if (cause != null) {
            System.err.println("Cause: " + cause.getClass().getName() + " - " + cause.getMessage());
            cause.printStackTrace();
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(message));
    }
    
    private String extractTargetType(String message) {
        // Try to extract the expected type from error message
        if (message.contains("LinkedHashMap")) {
            return "object (Map)";
        } else if (message.contains("List") || message.contains("ArrayList")) {
            return "array (List)";
        } else if (message.contains("String")) {
            return "string";
        } else if (message.contains("Integer") || message.contains("int")) {
            return "number (integer)";
        } else if (message.contains("Double") || message.contains("double") || message.contains("Float")) {
            return "number (decimal)";
        } else if (message.contains("Boolean") || message.contains("boolean")) {
            return "boolean";
        }
        return "object";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        ex.printStackTrace(); // Log error
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }
}

