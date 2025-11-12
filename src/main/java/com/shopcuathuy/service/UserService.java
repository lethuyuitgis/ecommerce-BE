package com.shopcuathuy.service;

import com.shopcuathuy.dto.UserAddressDTO;
import com.shopcuathuy.dto.UserDTO;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.UserAddress;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.UserAddressRepository;
import com.shopcuathuy.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;
    
    public UserDTO getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toDTO(user);
    }
    
    public UserDTO updateUserProfile(String userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getAvatarUrl() != null) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }
        
        user = userRepository.save(user);
        return toDTO(user);
    }
    
    public List<UserAddressDTO> getUserAddresses(String userId) {
        return userAddressRepository.findByUserId(userId).stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    public UserAddressDTO addAddress(String userId, UserAddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserAddress address = new UserAddress();
        address.setUser(user);
        address.setAddressType(UserAddress.AddressType.valueOf(addressDTO.getAddressType()));
        address.setFullName(addressDTO.getFullName());
        address.setPhone(addressDTO.getPhone());
        address.setProvince(addressDTO.getProvince());
        address.setDistrict(addressDTO.getDistrict());
        address.setWard(addressDTO.getWard());
        address.setStreet(addressDTO.getStreet());
        address.setIsDefault(addressDTO.getIsDefault());
        
        // If this is set as default, unset other defaults
        if (addressDTO.getIsDefault()) {
            userAddressRepository.findByUserId(userId).forEach(addr -> {
                if (addr.getIsDefault()) {
                    addr.setIsDefault(false);
                    userAddressRepository.save(addr);
                }
            });
        }
        
        address = userAddressRepository.save(address);
        return toAddressDTO(address);
    }
    
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setUserType(user.getUserType().name());
        return dto;
    }
    
    private UserAddressDTO toAddressDTO(UserAddress address) {
        UserAddressDTO dto = new UserAddressDTO();
        dto.setId(address.getId());
        dto.setAddressType(address.getAddressType().name());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setProvince(address.getProvince());
        dto.setDistrict(address.getDistrict());
        dto.setWard(address.getWard());
        dto.setStreet(address.getStreet());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}

