package com.shopcuathuy.service;

import com.shopcuathuy.dto.AuthRequest;
import com.shopcuathuy.dto.AuthResponse;
import com.shopcuathuy.dto.RegisterRequest;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.BadRequestException;
import com.shopcuathuy.exception.UnauthorizedException;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setUserType(User.UserType.CUSTOMER);
        user.setStatus(User.UserStatus.ACTIVE);
        
        user = userRepository.save(user);
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUserType().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType().name());
        
        return response;
    }
    
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getUserType().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType().name());
        
        return response;
    }
}

