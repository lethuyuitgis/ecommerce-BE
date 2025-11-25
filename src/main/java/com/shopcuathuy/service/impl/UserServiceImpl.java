package com.shopcuathuy.service.impl;

import com.shopcuathuy.dto.request.UpdateProfileRequestDTO;
import com.shopcuathuy.dto.response.UserProfileResponseDTO;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.UserRepository;
import com.shopcuathuy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileResponseDTO getProfile(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDTO updateProfile(String userId, UpdateProfileRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.fullName != null) user.setFullName(request.fullName);
        if (request.phone != null) user.setPhone(request.phone);
        if (request.avatarUrl != null) user.setAvatarUrl(request.avatarUrl);

        user = userRepository.save(user);
        return convertToProfile(user);
    }

    private UserProfileResponseDTO convertToProfile(User user) {
        UserProfileResponseDTO profile = new UserProfileResponseDTO();
        profile.id = user.getId();
        profile.email = user.getEmail();
        profile.fullName = user.getFullName();
        profile.phone = user.getPhone();
        profile.avatarUrl = user.getAvatarUrl();
        profile.userType = user.getUserType() != null ? user.getUserType().name() : null;
        // Note: Address is stored separately in UserAddress entity
        profile.address = null;
        return profile;
    }
}


