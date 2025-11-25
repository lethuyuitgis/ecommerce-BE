package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.UpdateProfileRequestDTO;
import com.shopcuathuy.dto.response.UserProfileResponseDTO;
import com.shopcuathuy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getProfile(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        UserProfileResponseDTO profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateProfile(
            @RequestBody UpdateProfileRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        UserProfileResponseDTO profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }
}
