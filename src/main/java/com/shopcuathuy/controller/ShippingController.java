package com.shopcuathuy.controller;

import com.shopcuathuy.api.ApiResponse;
import com.shopcuathuy.dto.request.ShippingCalculationRequestDTO;
import com.shopcuathuy.dto.response.ShippingAddressResponseDTO;
import com.shopcuathuy.dto.response.ShippingCalculationResponseDTO;
import com.shopcuathuy.dto.response.ShippingMethodResponseDTO;
import com.shopcuathuy.entity.ShippingMethod;
import com.shopcuathuy.entity.User;
import com.shopcuathuy.entity.UserAddress;
import com.shopcuathuy.exception.ResourceNotFoundException;
import com.shopcuathuy.repository.ShippingMethodRepository;
import com.shopcuathuy.repository.UserAddressRepository;
import com.shopcuathuy.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingMethodRepository shippingMethodRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    @Autowired
    public ShippingController(ShippingMethodRepository shippingMethodRepository,
                              UserAddressRepository userAddressRepository,
                              UserRepository userRepository) {
        this.shippingMethodRepository = shippingMethodRepository;
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<ShippingMethodResponseDTO>>> getShippingMethods() {
        List<ShippingMethod> methods = shippingMethodRepository.findByIsActiveTrue();
        List<ShippingMethodResponseDTO> methodDTOs = methods.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(methodDTOs));
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ShippingCalculationResponseDTO>> calculateShipping(
            @RequestBody ShippingCalculationRequestDTO request) {
        
        ShippingMethod method = shippingMethodRepository.findById(request.methodId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipping method not found"));

        // Calculate shipping fee based on weight, distance, etc.
        double baseFee = 30000.0; // Default base fee
        double weightFee = request.weight != null ? request.weight * 5000 : 0;
        double distanceFee = request.distance != null ? request.distance * 1000 : 0;
        double totalFee = baseFee + weightFee + distanceFee;

        ShippingCalculationResponseDTO calculation = new ShippingCalculationResponseDTO();
        calculation.methodId = method.getId();
        calculation.methodName = method.getName();
        calculation.estimatedDays = method.getMaxDeliveryDays() != null ? 
            method.getMaxDeliveryDays() : 5;
        calculation.fee = totalFee;
        calculation.currency = "VND";

        return ResponseEntity.ok(ApiResponse.success(calculation));
    }

    @GetMapping("/addresses")
    public ResponseEntity<ApiResponse<List<ShippingAddressResponseDTO>>> getAddresses(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
        List<ShippingAddressResponseDTO> addressDTOs = addresses.stream()
            .map(this::convertAddressToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(addressDTOs));
    }

    @PostMapping("/addresses")
    @Transactional
    public ResponseEntity<ApiResponse<ShippingAddressResponseDTO>> createAddress(
            @RequestBody ShippingAddressResponseDTO addressDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAddress address = new UserAddress();
        address.setId(UUID.randomUUID().toString());
        address.setUser(user);
        address.setFullName(addressDTO.name);
        address.setPhone(addressDTO.phone);
        address.setStreet(addressDTO.address);
        address.setProvince(addressDTO.province);
        address.setDistrict(addressDTO.district);
        address.setWard(addressDTO.ward);
        address.setIsDefault(addressDTO.isDefault != null ? addressDTO.isDefault : false);

        // If this is set as default, unset others
        if (address.getIsDefault()) {
            List<UserAddress> existingAddresses = userAddressRepository.findByUserId(userId);
            existingAddresses.forEach(a -> a.setIsDefault(false));
            userAddressRepository.saveAll(existingAddresses);
        }

        address = userAddressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.success(convertAddressToDTO(address)));
    }

    @PutMapping("/addresses/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<ShippingAddressResponseDTO>> updateAddress(
            @PathVariable String id,
            @RequestBody ShippingAddressResponseDTO updatedAddressDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        UserAddress address = userAddressRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        // Update fields
        if (updatedAddressDTO.name != null) address.setFullName(updatedAddressDTO.name);
        if (updatedAddressDTO.phone != null) address.setPhone(updatedAddressDTO.phone);
        if (updatedAddressDTO.address != null) address.setStreet(updatedAddressDTO.address);
        if (updatedAddressDTO.province != null) address.setProvince(updatedAddressDTO.province);
        if (updatedAddressDTO.district != null) address.setDistrict(updatedAddressDTO.district);
        if (updatedAddressDTO.ward != null) address.setWard(updatedAddressDTO.ward);
        if (updatedAddressDTO.isDefault != null) {
            if (updatedAddressDTO.isDefault) {
                List<UserAddress> addresses = userAddressRepository.findByUserId(userId);
                addresses.forEach(a -> a.setIsDefault(false));
                userAddressRepository.saveAll(addresses);
            }
            address.setIsDefault(updatedAddressDTO.isDefault);
        }

        address = userAddressRepository.save(address);
        return ResponseEntity.ok(ApiResponse.success(convertAddressToDTO(address)));
    }

    @DeleteMapping("/addresses/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("User not authenticated"));
        }

        UserAddress address = userAddressRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403)
                .body(ApiResponse.error("Access denied"));
        }

        userAddressRepository.delete(address);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private ShippingMethodResponseDTO convertToDTO(ShippingMethod method) {
        ShippingMethodResponseDTO dto = new ShippingMethodResponseDTO();
        dto.id = method.getId();
        dto.name = method.getName();
        dto.description = method.getDescription();
        dto.baseFee = 30000.0; // Default, not stored in entity
        dto.estimatedDays = method.getMaxDeliveryDays() != null ? 
            method.getMaxDeliveryDays() : 5;
        dto.displayOrder = 0; // Not stored in entity
        dto.isActive = method.getIsActive();
        return dto;
    }

    private ShippingAddressResponseDTO convertAddressToDTO(UserAddress address) {
        ShippingAddressResponseDTO dto = new ShippingAddressResponseDTO();
        dto.id = address.getId();
        dto.userId = address.getUser() != null ? address.getUser().getId() : null;
        dto.name = address.getFullName();
        dto.phone = address.getPhone();
        dto.address = address.getStreet();
        dto.province = address.getProvince();
        dto.district = address.getDistrict();
        dto.ward = address.getWard();
        dto.isDefault = address.getIsDefault();
        return dto;
    }
}

