package com.shopcuathuy.controller;

import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.UserAddressDTO;
import com.shopcuathuy.dto.UserDTO;
import com.shopcuathuy.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import com.shopcuathuy.dto.ApiResponse;
import com.shopcuathuy.dto.ProductDTO;
import com.shopcuathuy.service.WishlistService;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(@RequestHeader("X-User-Id") String userId) {
        UserDTO user = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserDTO userDTO) {
        UserDTO updated = userService.updateUserProfile(userId, userDTO);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updated));
    }
    
    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<UserAddressDTO>>> getAddresses(@RequestHeader("X-User-Id") String userId) {
        List<UserAddressDTO> addresses = userService.getUserAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(addresses));
    }
    
    @PostMapping("/addresses")
    public ResponseEntity<ApiResponse<UserAddressDTO>> addAddress(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody UserAddressDTO addressDTO) {
        UserAddressDTO address = userService.addAddress(userId, addressDTO);
        return ResponseEntity.ok(ApiResponse.success("Address added", address));
    }
}

