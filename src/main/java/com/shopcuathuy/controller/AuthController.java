package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.FacebookLoginRequestDTO;
import com.shopcuathuy.dto.request.GoogleLoginRequestDTO;
import com.shopcuathuy.dto.request.LoginRequestDTO;
import com.shopcuathuy.dto.request.RefreshTokenRequestDTO;
import com.shopcuathuy.dto.request.RegisterRequestDTO;
import com.shopcuathuy.dto.response.AuthResponseDTO;
import com.shopcuathuy.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refresh(@RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> loginWithGoogle(@RequestBody GoogleLoginRequestDTO request) {
        AuthResponseDTO response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> loginWithFacebook(@RequestBody FacebookLoginRequestDTO request) {
        AuthResponseDTO response = authService.loginWithFacebook(request.accessToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
