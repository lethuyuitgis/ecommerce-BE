package com.shopcuathuy.service;

import com.shopcuathuy.dto.request.UpdateProfileRequestDTO;
import com.shopcuathuy.dto.response.UserProfileResponseDTO;

public interface UserService {
    UserProfileResponseDTO getProfile(String userId);
    UserProfileResponseDTO updateProfile(String userId, UpdateProfileRequestDTO request);
}


