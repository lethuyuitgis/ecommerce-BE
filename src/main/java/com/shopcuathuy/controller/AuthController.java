package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.AuthRequest;
import com.shopcuathuy.dto.AuthResponse;
import com.shopcuathuy.dto.RegisterRequest;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.service.AuthService;
import com.shopcuathuy.service.OAuthService;
import jakarta.validation.Valid;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final OAuthService oAuthService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
    
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            throw new BadRequestException("Google ID token is required");
        }
        AuthResponse response = oAuthService.loginWithGoogle(idToken);
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }
    
    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithFacebook(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        if (accessToken == null || accessToken.isEmpty()) {
            throw new BadRequestException("Facebook access token is required");
        }
        AuthResponse response = oAuthService.loginWithFacebook(accessToken);
        return ResponseEntity.ok(ApiResponse.success("Facebook login successful", response));
    }
}

