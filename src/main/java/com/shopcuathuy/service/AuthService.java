package com.shopcuathuy.service;

import com.shopcuathuy.dto.request.GoogleLoginRequestDTO;
import com.shopcuathuy.dto.request.LoginRequestDTO;
import com.shopcuathuy.dto.request.RefreshTokenRequestDTO;
import com.shopcuathuy.dto.request.RegisterRequestDTO;
import com.shopcuathuy.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO register(RegisterRequestDTO request);
    void logout(String token);
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
    AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request);
    AuthResponseDTO loginWithFacebook(String accessToken);
}
