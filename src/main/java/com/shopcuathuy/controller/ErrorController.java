package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller để xử lý các request không phải API
 * Trả về 404 cho các path không tồn tại
 */
@RestController
@CrossOrigin(origins = "*")
public class ErrorController {

    /**
     * Handle các request đến path không phải API
     * Trả về 404 với message rõ ràng
     */
    @RequestMapping(value = {
        "/cart",
        "/orders",
        "/products",
        "/categories",
        "/users",
        "/wishlist",
        "/checkout",
        "/profile",
        "/seller",
        "/admin"
    }, method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<ApiResponse<Object>> handleNonApiPaths() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("API endpoint not found. Please use /api/* endpoints."));
    }
    
    /**
     * Handle các request đến /auth/* paths (không phải /api/auth/*)
     */
    @RequestMapping(value = "/auth/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<ApiResponse<Object>> handleAuthPaths() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("API endpoint not found. Please use /api/auth/* endpoints."));
    }
}

